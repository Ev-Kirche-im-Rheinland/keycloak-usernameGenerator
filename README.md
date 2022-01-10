# Keycloak username generator

this keycloak module, allows adding a custom prefix to all newly created users,
by offering a Replacement to the RegistrationUserCreation Authenticator.

# Install

1. copy the jar file to /opt/jboss/keycloak/standalone/deployments/
2. create a new authentication flow by e.g. copying the Registration Flow
3. remove the RegistrationUserCreation Execution form the flow
4. add RegistrationUsernameGenerator to the flow
5. move RegistrationUsernameGenerator to the top of the Form Flow
5. (optional) set the prefix, by configuring the RegistrationUsernameGenerator Execution
