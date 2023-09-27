#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer
from datetime import datetime
import time
import json as j
import subprocess as p
import random

hostName = "0.0.0.0"
serverPort = 8080


def invalid_format():
    return '{"message": "invalid request format; missing fields"}'


def log(msg):
    f = open('/app/server.log', 'a')
    f.write(msg + "\n")
    f.close()


def write_to_file(filepath, text):
    f = open(filepath, 'w+')
    f.write(text)
    f.close()


# POST REQUESTS


def run(json):
    if 'code' in json and 'test' in json and 'image' in json:
        tmp_dir = '/input/' + str(random.random())
        p.run(['mkdir', tmp_dir])
        write_to_file(tmp_dir + '/code', json['code'])
        write_to_file(tmp_dir + '/test', json['test'])
        output = p.run([
            'docker', 'run', '-v',
            (tmp_dir + '/:/input/'), '--rm', json['image']
        ],
                       universal_newlines=True,
                       capture_output=True).stdout
        p.run(['rm', '-rf', tmp_dir])
        json = {'output': output}
        return (200, j.dumps(json))
    else:
        return (400, invalid_format())


# GET REQUESTS


def home():
    return (200, '{"message": "service online"}')


get_reqs = {'/': lambda: home()}
post_reqs = {'/run': lambda json: run(json)}


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
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass
    except Exception as e:
        log("Error 2")
        log(e)

    log("-- dying --")
    webServer.server_close()
    print("Server stopped.")