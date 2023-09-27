#!/usr/bin/env python
import os
import requests
import pytest
import re

# This is a very hasty start of our integration test suite; mostly to document the logic for initial tests

AUTH0_CLIENT_ID = 'oUP4aphuLpw2ydBg8xYK5YH1P3tkuLjs'
USERNAME = 'customersupport@masterypath.io'
PASSWORD = '6t#4MmYy@QEg'
INVITE_EMAIL = 'tonymichaelson@protonmail.com'
ORG_ID = '57d00d57-28de-410c-b1cf-d97c4ccba08d'
REST_API_HOST = 'http://localhost:9000'


def getToken():
    r = requests.post("https://masterypath.auth0.com/oauth/token", data={
        'username': USERNAME,
        'password': PASSWORD,
        'client_id': os.environ['AUTH0_CLIENT_ID'],
        'client_secret': os.environ['AUTH0_CLIENT_SECRET'],
        'audience': os.environ['AUTH0_AUDIENCE'],
        'grant_type': 'password'
    })
    assert r.status_code == 200
    TOKEN = r.json()['access_token']
    if re.search("\\w+\\.\\w+\\.\\w+", TOKEN):
        assert True
        return TOKEN
    else:
        assert False


TOKEN = getToken()
HEADERS = {'Content-Type': 'application/json', 'AUTHORIZATION': 'Bearer ' + TOKEN}


def getProfileID():
    r = requests.get(REST_API_HOST + "/member/profile", headers=HEADERS)
    assert r.status_code is 200
    if r.json()['profile']['id']:
        profileID = r.json()['profile']['id']
        assert True
        return profileID
    else:
        assert False


def getMapID():
    r = requests.get(REST_API_HOST + "/org/" + ORG_ID + "/mymaps", headers=HEADERS)
    assert r.status_code is 200
    if r.json()[0]['id']:
        mapID = r.json()[0]['id']
        assert True
        return mapID
    else:
        assert False


def getMapByID(mapID):
    r = requests.get(REST_API_HOST + "/org/" + ORG_ID + "/map/" + mapID + "/segment/" + mapID, headers=HEADERS)
    assert r.status_code is 200
    if r.json()['attr']['id']:
        rootID = r.json()['attr']['id']
        path = r.json()['attr']['path']
        assert True
        return (rootID, path)
    else:
        assert False


def getRoleID():
    r = requests.get(REST_API_HOST + "/org/" + ORG_ID + "/roles", headers=HEADERS)
    assert r.status_code is 200
    if r.json()[0]['id']:
        roleID = r.json()[0]['id']
        assert True
        return roleID
    else:
        assert False


def getInviteID(roleID):
    r = requests.post(REST_API_HOST + "/org/" + ORG_ID + "/role/" + roleID + "/invite",
                      json={'emailAddress': INVITE_EMAIL}, headers=HEADERS)
    assert r.status_code is 200
    if r.json()['inviteID']:
        inviteID = r.json()['inviteID']
        assert True
        return inviteID
    else:
        assert False


def addNode(name, parentID, mapID, nodeNumber, order):
    return requests.post(REST_API_HOST + "/org/" + ORG_ID + "/map/" + mapID + "/segment/" + mapID + "/node",
                      json={
                          "name":             name,
                          "parentID":         parentID,
                          "nodeType":         "category",
                          "nodeNumber":		  nodeNumber,
                          "order":            order
                      }, headers=HEADERS)


def addRoleMember(roleID, profileID):
    return requests.post(REST_API_HOST + "/org/" + ORG_ID + "/role/" + roleID + "/member/" + profileID, headers=HEADERS)


def joinRole(inviteID):
    return requests.post(REST_API_HOST + "/invite/" + inviteID + "/join", headers=HEADERS)


def main():
    profileID = getProfileID()
    mapID = getMapID()
    (nodeID, path) = getMapByID(mapID)

    # PSQL constraint checks
    r = addNode("Math", nodeID, mapID, 1, 1)
    assert r.status_code == 500
    assert re.match('.*map_nodenumber.*', str(r.content))

    r = addNode("Math", nodeID, mapID, 22, 20)
    assert r.status_code == 200
    assert re.match('.*"id":.*', str(r.content))

    r = addNode("Science", nodeID, mapID, 23, 20)
    assert r.status_code == 500
    assert re.match('.*parent_order.*', str(r.content))

    r = addNode("Math", nodeID, mapID, 24, 21)
    assert r.status_code == 500
    assert re.match('.*title_parent.*', str(r.content))

    roleID = getRoleID()

    r1 = addRoleMember(roleID, profileID)
    r2 = addRoleMember(roleID, profileID)
    assert r1.status_code == 500 or 200  # fist request may succeed
    assert r2.status_code == 500

    print("PSQL CONSTRAINTS TESTS PASSED")

main()
