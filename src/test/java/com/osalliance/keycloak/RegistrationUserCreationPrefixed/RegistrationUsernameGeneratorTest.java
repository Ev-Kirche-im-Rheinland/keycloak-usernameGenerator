package com.osalliance.keycloak.RegistrationUserCreationPrefixed;


import org.keycloak.http.HttpRequest;
import org.junit.Test;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authorization.policy.evaluation.Realm;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.keycloak.userprofile.Attributes;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class RegistrationUsernameGeneratorTest {

    @Test
    public void test(){
        UserProfile userProfile = Mockito.mock(UserProfile.class);
        ValidationContext context = Mockito.mock(ValidationContext.class);
        Attributes attributes = Mockito.mock(Attributes.class);
        HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
        //FormContext context = Mockito.mock(FormContext.class);
        AuthenticatorConfigModel authenticatorConfigModel = Mockito.mock(AuthenticatorConfigModel.class);
        RealmModel realm = Mockito.mock(RealmModel.class);
        KeycloakSession keycloakSession = Mockito.mock(KeycloakSession.class);
        UserProvider userProvider = Mockito.mock(UserProvider.class);
        UserModel userModel = Mockito.mock(UserModel.class);
        AuthenticationSessionModel authenticationSessionModel = Mockito.mock(AuthenticationSessionModel.class);
        ClientModel clientModel = Mockito.mock(ClientModel.class);
        UserModel alreadyExistingUser = Mockito.mock(UserModel.class);
        UserProfileProvider userProfileProvider = Mockito.mock(UserProfileProvider.class);

        MultivaluedMap<String,String> formData = Mockito.mock(MultivaluedMap.class);

        EventBuilder eventBuilder = mock(EventBuilder.class);

        String usernamePrefix = "extra.";

        Map<String,String> config = new HashMap<>();
        config.put(RegistrationUsernameGeneratorFactory.PROVIDER_PREFIX,"extra.");

        String email = "max.mustermann@test.tld";
        String firstName = "max";
        String lastName = "mustermann";
        String username = "max.mustermann";
        String prefixedUsername = usernamePrefix+firstName+"."+lastName;


        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        when(eventBuilder.detail(Details.REGISTER_METHOD, "form")).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.EMAIL, email)).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.USERNAME, username)).thenReturn(eventBuilder);

        when(eventBuilder.detail(Details.REDIRECT_URI, eq(anyString()) )).thenReturn(eventBuilder);
        when(eventBuilder.detail(Details.USERNAME, eq(anyString()))).thenReturn(eventBuilder);

        when(context.getEvent()).thenReturn(eventBuilder);
        when(context.getHttpRequest()).thenReturn(httpRequest);
        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(userProfile.getAttributes()).thenReturn(attributes);

        when(attributes.getFirstValue(UserModel.EMAIL)).thenReturn(email);
        when(attributes.getFirstValue(UserModel.USERNAME)).thenReturn(username);
        when(attributes.getFirstValue(UserModel.FIRST_NAME)).thenReturn(firstName);
        when(attributes.getFirstValue(UserModel.LAST_NAME)).thenReturn(lastName);

        when(formData.getFirst(UserModel.EMAIL)).thenReturn(email);
        when(formData.getFirst(UserModel.USERNAME)).thenReturn(username);
        when(formData.getFirst(UserModel.FIRST_NAME)).thenReturn(firstName);
        when(formData.getFirst(UserModel.LAST_NAME)).thenReturn(lastName);

        //when(attributes.toUserProfile(formData)).thenReturn(userProfile);
        when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfigModel);
        when(authenticatorConfigModel.getConfig()).thenReturn(config);
        when(context.getRealm()).thenReturn(realm);
        when(realm.isRegistrationEmailAsUsername()).thenReturn(false);
        when(context.getSession()).thenReturn(keycloakSession);
        when(keycloakSession.users()).thenReturn(userProvider);
        when(userProvider.addUser(any(), any())).thenReturn(userModel);
        when(context.getAuthenticationSession()).thenReturn(authenticationSessionModel);
        when(context.newEvent()).thenReturn(eventBuilder);
        when(authenticationSessionModel.getClient()).thenReturn(clientModel);
        when(clientModel.getClientId()).thenReturn("xyz");
        when(authenticationSessionModel.getRedirectUri()).thenReturn("xyz");
        when(userProvider.getUserByUsername(any(RealmModel.class), any(String.class))).thenReturn(null);
        when(keycloakSession.getProvider(any())).thenReturn(userProfileProvider);
        when(userProfileProvider.create(any(UserProfileContext.class),any(MultivaluedMap.class))).thenReturn(userProfile);
        when(userProfile.create()).thenReturn(userModel);
        //context.getSession().users().getUserByUsername(username, context.getRealm());



        doNothing().when(authenticationSessionModel).setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, prefixedUsername);

        RegistrationUsernameGenerator registrationUserCreationPrefixed = new RegistrationUsernameGenerator();

        registrationUserCreationPrefixed.success(context);

        verify(context.getSession().users().addUser(realm,prefixedUsername),times(1));

    }
}
