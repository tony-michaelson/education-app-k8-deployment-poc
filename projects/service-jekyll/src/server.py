#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
import time
import json as j
import subprocess as p
import os

hostName = "0.0.0.0"
serverPort = 8080


def invalid_format():
    return '{"message": "invalid request format; missing fields"}'


def log(msg):
    f = open('/app/server.log', 'a')
    f.write(msg + "\n")
    f.close()


# POST REQUESTS


def build(json):
    if 'theme' in json and 'domain' in json:
        jekyll_theme_folder = '/home/jekyll/' + json['theme']
        jekyll_site_folder = '/home/jekyll/' + json['theme'] + '/_site'
        site_folder = '/sites/' + json['domain']
        build_files = '/sites/.build_files/' + json['domain'].replace(
            "staging.", "")
        p.run(['rm', '-rf', jekyll_site_folder])
        if os.path.isdir(build_files):
            p.run([
                'mv', build_files + '/_config.yml', jekyll_theme_folder + "/"
            ])
        output = p.run(['jekyll', 'build'],
                       universal_newlines=True,
                       capture_output=True,
                       cwd='/home/jekyll/' + json['theme']).stdout
        p.run(['rm', '-rf', site_folder])
        p.run(['mv', jekyll_site_folder, site_folder])
        log(output)
        return (200, '{"output": "%s"}' % output)
    else:
        return (400, invalid_format())


def publish(json):
    if 'staging_domain' in json and 'domain' in json:
        staging_folder = json['staging_domain']
        site_folder = json['domain']
        p.run(['rm', '-rf', site_folder], cwd='/sites/')
        p.run(['cp', '-r', staging_folder, site_folder], cwd='/sites/')
        log("Site published")
        return (200, '{"output": "%s"}' % "Site published")
    else:
        return (400, invalid_format())


# GET REQUESTS


def home():
    return (200, '{"message": "service online"}')


get_reqs = {'/': lambda: home()}
post_reqs = {
    '/build': lambda json: build(json),
    '/publish': lambda json: publish(json)
}


class MyServer(BaseHTTPRequestHandler):
    def not_found(self):
        self.send_response(404)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(bytes('{"message": "path not found"}', "utf-8"))

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
            self.not_found()

    def do_POST(self):
        self.protocol_version = 'HTTP/1.1'
        if (self.path in post_reqs):
            try:
                log("-- handling %s --" % self.path)
                content_len = int(self.headers.get('Content-Length'))
                post_body = self.rfile.read(content_len)
                json = j.loads(post_body)
                r = post_reqs[self.path](json)
                self.send_response(r[0])
                self.send_header("Content-type", "application/json")
                self.send_header("Content-length", len(r[1]))
                self.end_headers()
                self.wfile.write(bytes("%s" % r[1], "utf-8"))
                log(("-- end handling %s --" % (self.path)))
            except Exception as e:
                log("Error")
                log(e)
        else:
            self.not_found()


if __name__ == "__main__":
    webServer = HTTPServer((hostName, serverPort), MyServer)
    log(("Server started http://%s:%s" % (hostName, serverPort)))
    print("Server started http://%s:%s" % (hostName, serverPort))

    try:
        build({'domain': 'container-prep.masterypath.io', 'theme': 'memoirs'})
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass
    except Exception as e:
        log("Error 2")
        log(e)

    log("-- dying --")
    webServer.server_close()
    print("Server stopped.")