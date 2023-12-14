#!/usr/bin/env python3
# Keycloak SAML2 Metadata Importer
# Copyright (C) 2023 Armin Felder
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import hashlib
import json
import os
import io
import sqlite3
import sys
import xmlsec
from datetime import datetime, timezone

import keycloak.exceptions
from lxml import etree

import ecs_logging
import logging
import requests as requests
from keycloak import KeycloakAdmin,KeycloakGetError
from keycloak.exceptions import raise_error_from_response

logger = logging.getLogger("app")
logger.setLevel(logging.INFO)
handler = logging.StreamHandler()
handler.setFormatter(ecs_logging.StdlibFormatter())
logger.addHandler(handler)


keycloak_admin = KeycloakAdmin(server_url="http://127.0.0.1:8080/",
                                       username="admin",
                                       password="admin",
                                       realm_name="master",
                                       verify=True)

res=keycloak_admin.connection.raw_get("realms/master/username-generator-restapi/hello")
print(res)
print(res.text)

res=keycloak_admin.connection.raw_get("realms/master/username-generator-restapi/hello-2")
print(res)
print(res.text)

res=keycloak_admin.connection.raw_get("admin/realms/master/username-generator-admin-restapi")
print(res)
print(res.text)

res=keycloak_admin.connection.raw_post("admin/realms/master/username-generator-admin-restapi", 
	data=json.dumps({
		"firstName":"Joseph",
		"lastName":"Wenninger"
	})
)
print(res)
print(res.text)



res=keycloak_admin.connection.raw_post("admin/realms/master/users", 
	data=json.dumps({
		"firstName":"Joseph",
		"lastName":"Wenninger",
                "username":"XXX"
	})
)
print(res)
print(res.text)

