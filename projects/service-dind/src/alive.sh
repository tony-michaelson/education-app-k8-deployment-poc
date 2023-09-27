#!/bin/bash
netstat -anp | grep '0.0.0.0:8080' && docker info