#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
import time
import json as j
import subprocess as p
import pika
import sys
import os
import re
import boto3

MQ_HOST = os.environ['MQ_HOST']
MQ_USER = os.environ['MQ_USER']
MQ_PASS = os.environ['MQ_PASS']
SITES_URL = os.environ['SITES_URL']
HOSTED_ZONE_ID = os.environ['HOSTED_ZONE_ID']

hostName = "0.0.0.0"
serverPort = 8080

client = boto3.client('route53')


def reload_nginx():
    credentials = pika.PlainCredentials(MQ_USER, MQ_PASS)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=MQ_HOST,
                                  port=5672,
                                  virtual_host='/',
                                  credentials=credentials))
    channel = connection.channel()
    channel.exchange_declare(exchange='nginx_host', exchange_type='fanout')
    message = '{"task": "reload"}'
    channel.basic_publish(exchange='nginx_host', routing_key='', body=message)
    print(" [x] Sent %r" % message)
    connection.close()


def invalid_format():
    return '{"message": "invalid request format; missing fields"}'


def not_found(self):
    self.send_response(404)
    self.send_header("Content-type", "application/json")
    self.end_headers()
    self.wfile.write(bytes('{"message": "path not found"}', "utf-8"))


# HELPER FUNCTIONS


def create_aws_dns_entry(domain):
    ChangeBatch = {
        'Changes': [{
            'Action': 'UPSERT',
            'ResourceRecordSet': {
                'Name': domain,
                'Type': 'CNAME',
                'TTL': 300,
                'ResourceRecords': [
                    {
                        'Value': SITES_URL
                    },
                ]
            }
        }],
    }
    r = client.change_resource_record_sets(HostedZoneId='/hostedzone/' +
                                           HOSTED_ZONE_ID,
                                           ChangeBatch=ChangeBatch)
    return r['ResponseMetadata']['HTTPStatusCode']


def delete_aws_dns_entry(domain):
    ChangeBatch = {
        'Changes': [{
            'Action': 'DELETE',
            'ResourceRecordSet': {
                'Name': domain,
                'Type': 'CNAME',
                'TTL': 300,
                'ResourceRecords': [
                    {
                        'Value': SITES_URL
                    },
                ]
            }
        }],
    }
    r = client.change_resource_record_sets(HostedZoneId='/hostedzone/' +
                                           HOSTED_ZONE_ID,
                                           ChangeBatch=ChangeBatch)
    return r['ResponseMetadata']['HTTPStatusCode']


def create_nginx_config(domain):
    port = '80'
    config_filepath = '/etc/nginx/' + port + '/' + domain
    f = open('/etc/nginx/template_' + port + '.conf', "r")
    template = f.read()
    f.close()
    template = re.sub('{{domain}}', domain, template)
    template = re.sub('{{site_folder}}', domain, template)
    f = open(config_filepath, "w+")
    f.write(template)
    f.close()
    output = p.run(['nginx', '-t'],
                   universal_newlines=True,
                   capture_output=True).stderr
    if re.match('.*syntax is ok.*', output, flags=re.DOTALL) and re.match(
            '.*test is successful.*', output, flags=re.DOTALL):
        return (True, output)
    else:
        p.run(['rm', config_filepath])
        return (False, output)


def delete_nginx_config(domain):
    port = '80'
    config_filepath = '/etc/nginx/' + port + '/' + domain
    p.run(['rm', config_filepath])
    output = p.run(['stat', config_filepath],
                   universal_newlines=True,
                   capture_output=True).stderr
    if re.match('.*No such file.*', output, flags=re.DOTALL):
        return (True, "Nginx Config Deleted")
    else:
        return (False, "Unable to delete Nginx Config: " + config_filepath)


def _create_site_content(domain):
    p.run(['mkdir', '/sites/' + domain])
    p.run(['mkdir', '/sites/.build_files/' + domain])
    p.run([
        'cp', '/app/hello-kitty.jpg', '/sites/' + domain + '/hello-kitty.jpg'
    ])
    p.run(['cp', '/app/welcome.html', '/sites/' + domain + '/index.html'])
    r = create_nginx_config(domain)
    if r[0] is True:
        reload_nginx()
        output = p.run(['ls', '-l', domain],
                       universal_newlines=True,
                       capture_output=True,
                       cwd='/sites/').stdout
        return (200, output)
    else:
        p.run(['rm', '-rf', '/sites/' + domain])
        return (500, r[1])


def _create_site(domain):
    response = create_aws_dns_entry(domain)
    if response == 200:
        return _create_site_content(domain)
    else:
        (response, "DNS RECORD CREATION NOT SUCCESSFUL")


def _delete_site_content(domain):
    p.run(['rm', '-rf', '/sites/' + domain])
    r = delete_nginx_config(domain)
    if r[0] is True:
        reload_nginx()
        return (200, "Site Removed")
    else:
        return (500, r[1])


def _delete_site(domain):
    response = delete_aws_dns_entry(domain)
    if response == 200:
        return _delete_site_content(domain)
    else:
        (response, "DNS RECORD CREATION NOT SUCCESSFUL")


# POST REQUESTS


def create_site(json):
    if 'domain' in json:
        r = _create_site(json['domain'])
        return (r[0], '{"output": "%s"}' % r[1])
    else:
        return (400, invalid_format())


def delete_site(json):
    if 'domain' in json:
        r = _delete_site(json['domain'])
        return (r[0], '{"output": "%s"}' % r[1])
    else:
        return (400, invalid_format())


# GET REQUESTS


def home():
    return (200, '{"message": "service online"}')


get_reqs = {'/': lambda: home()}
post_reqs = {
    '/create_site': lambda json: create_site(json),
    '/delete_site': lambda json: delete_site(json)
}


class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.protocol_version = 'HTTP/1.1'
        if (self.path in get_reqs):
            r = get_reqs[self.path]()
            self.send_response(r[0])
            self.send_header("Content-type", "application/json")
            self.send_header("Content-length", len(r[1]))
            self.end_headers()
            self.wfile.write(bytes("%s" % r[1], "utf-8"))
        else:
            not_found(self)

    def do_POST(self):
        self.protocol_version = 'HTTP/1.1'
        if (self.path in post_reqs):
            content_len = int(self.headers.get('Content-Length'))
            post_body = self.rfile.read(content_len)
            json = j.loads(post_body)
            r = post_reqs[self.path](json)
            self.send_response(r[0])
            self.send_header("Content-type", "application/json")
            self.send_header("Content-length", len(r[1]))
            self.end_headers()
            self.wfile.write(bytes("%s" % r[1], "utf-8"))
        else:
            not_found(self)


if __name__ == "__main__":
    webServer = HTTPServer((hostName, serverPort), MyServer)
    print("Server started http://%s:%s" % (hostName, serverPort))

    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped.")