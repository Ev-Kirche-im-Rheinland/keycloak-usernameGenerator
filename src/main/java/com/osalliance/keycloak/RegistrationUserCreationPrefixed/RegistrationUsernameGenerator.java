package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.keycloak.authentication.DefaultAuthenticationFlow;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

public class RegistrationUsernameGenerator implements FormAction {

    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        KeycloakSession session = context.getSession();
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_USER_CREATION, formData);
        String email = profile.getAttributes().getFirstValue(UserModel.EMAIL);

        String username = profile.getAttributes().getFirstValue(UserModel.USERNAME);
        String firstName = profile.getAttributes().getFirstValue(UserModel.FIRST_NAME);
        String lastName = profile.getAttributes().getFirstValue(UserModel.LAST_NAME);
        context.getEvent().detail(Details.EMAIL, email);

        context.getEvent().detail(Details.USERNAME, username);
        context.getEvent().detail(Details.FIRST_NAME, firstName);
        context.getEvent().detail(Details.LAST_NAME, lastName);

        try {
            profile.validate();
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());

            if (pve.hasError(Messages.EMAIL_EXISTS)) {
                context.error(Errors.EMAIL_IN_USE);
            } else if (pve.hasError(Messages.MISSING_EMAIL, Messages.INVALID_EMAIL)) {
                context.error(Errors.INVALID_REGISTRATION);
            }

            context.validationError(formData, errors);
            return;
        }
        context.success();
    }

    @Override
    public void success(FormContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String email = formData.getFirst(UserModel.EMAIL);
        String username = formData.getFirst(UserModel.USERNAME);


        String firstName = formData.getFirst(UserModel.FIRST_NAME);
        String lastName = formData.getFirst(UserModel.LAST_NAME);

        username = firstName+"."+lastName;

        if(context.getAuthenticatorConfig().getConfig() != null && context.getAuthenticatorConfig().getConfig().containsKey(RegistrationUsernameGeneratorFactory.PROVIDER_PREFIX)){
            username = context.getAuthenticatorConfig().getConfig().get(RegistrationUsernameGeneratorFactory.PROVIDER_PREFIX)+"."+username;
        }


        UserModel alreadyExistingUser = context.getSession().users().getUserByUsername(context.getRealm(),username);

        if(alreadyExistingUser != null){
           int index = 2;
           while (true){
               String searchUsername = username+index;
               alreadyExistingUser = context.getSession().users().getUserByUsername(context.getRealm(), searchUsername);

               if(alreadyExistingUser == null){
                   break;
               }
               index++;

           }
           username = username+index;

        }

        EventBuilder event = context.getEvent();
        event.detail(Details.USERNAME, username);
        event.detail(Details.REGISTER_METHOD, "form");
        event.detail(Details.EMAIL, email);

        KeycloakSession session = context.getSession();


        // dirty hack, patches the form data, because it will be reused during validation later
        ArrayList<String> usernames = new ArrayList<>();
        usernames.add(username);
        formData.put(UserModel.USERNAME,usernames);

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_USER_CREATION, formData);
        UserModel user = profile.create();

        user.setEnabled(true);

        context.setUser(user);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        EventBuilder event2 =context.getEvent();
        event2.client(context.getAuthenticationSession().getClient().getClientId());
        event2.detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri());
        event2.detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }

    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }


    @Override
    public void close() {

    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

}
