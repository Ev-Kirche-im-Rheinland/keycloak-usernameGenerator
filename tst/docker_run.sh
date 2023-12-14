docker run --rm -p 127.0.0.1:8080:8080  -e KEYCLOAK_ADMIN=admin -v /Users/jowenn/.m2/repository/com/osalliance/keycloak/RegistrationUserCreationPrefixed/RegistrationUsername/23.0.1/RegistrationUsername-23.0.1.jar:/opt/keycloak/providers/RegistrationUsername-23.0.1.jar -e KEYCLOAK_ADMIN_PASSWORD=admin -e KEYCLOAK_LOGLEVEL=DEBUG -e WILDFLY_LOGLEVEL=DEBUG quay.io/keycloak/keycloak:23.0.1 start-dev

