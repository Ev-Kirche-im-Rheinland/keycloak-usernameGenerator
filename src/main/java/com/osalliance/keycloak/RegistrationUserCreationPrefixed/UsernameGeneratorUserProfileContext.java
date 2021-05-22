package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.profile.DefaultUserProfileContext;
import org.keycloak.userprofile.profile.representations.IdpUserProfile;
import org.keycloak.userprofile.profile.representations.UserModelUserProfile;
import org.keycloak.userprofile.validation.UserUpdateEvent;

public class UsernameGeneratorUserProfileContext implements UserProfileContext {
    private UserProfile currentUserProfile;
    private UserUpdateEvent userUpdateEvent;

    private UsernameGeneratorUserProfileContext(UserUpdateEvent userUpdateEvent, UserProfile currentUserProfile) {
        this.userUpdateEvent = userUpdateEvent;
        this.currentUserProfile = currentUserProfile;
    }

    public static UsernameGeneratorUserProfileContext forIdpReview(SerializedBrokeredIdentityContext currentUser) {
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.IdpReview, new IdpUserProfile(currentUser));
    }

    public static UsernameGeneratorUserProfileContext forUpdateProfile(UserModel currentUser) {
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.UpdateProfile, new UserModelUserProfile(currentUser));
    }

    public static UsernameGeneratorUserProfileContext forAccountService(UserModel currentUser) {
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.Account, new UserModelUserProfile(currentUser));
    }

    public static UsernameGeneratorUserProfileContext forRegistrationUserCreation() {
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.RegistrationUserCreation, null);
    }

    public static UsernameGeneratorUserProfileContext forRegistrationProfile() {
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.RegistrationProfile, null);
    }

    /**
     * @param currentUser if this is null, then we're creating new user. If it is not null, we're updating existing user
     * @return user profile context for the validation of user when called from admin REST API
     */
    public static UsernameGeneratorUserProfileContext forUserResource(UserModel currentUser) {
        UserProfile currentUserProfile = currentUser == null ? null : new UserModelUserProfile(currentUser);
        return new UsernameGeneratorUserProfileContext(UserUpdateEvent.UserResource, currentUserProfile);
    }

    @Override
    public UserProfile getCurrentProfile() {
        return currentUserProfile;
    }

    @Override
    public  UserUpdateEvent getUpdateEvent(){
        return  userUpdateEvent;
    }
}
