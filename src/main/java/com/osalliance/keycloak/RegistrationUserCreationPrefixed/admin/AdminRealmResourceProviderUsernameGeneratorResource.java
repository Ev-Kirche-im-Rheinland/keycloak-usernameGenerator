package com.osalliance.keycloak.RegistrationUserCreationPrefixed.admin;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.UserPermissionEvaluator;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.common.ClientConnection;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.models.ModelException;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.models.GroupModel;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.keycloak.policy.PasswordPolicyNotMetException;
import org.keycloak.common.Profile;

import java.util.Optional;
import org.jboss.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.keycloak.models.GroupModel;
import static org.keycloak.userprofile.UserProfileContext.USER_API;
import static org.keycloak.models.utils.KeycloakModelUtils.findGroupByPath;

import com.osalliance.keycloak.RegistrationUserCreationPrefixed.UsernameGenerator;


public class AdminRealmResourceProviderUsernameGeneratorResource {
    private static final Logger logger = Logger.getLogger(AdminRealmResourceProviderUsernameGeneratorResource.class);
    private static final String SEARCH_ID_PARAMETER = "id:";

    protected final RealmModel realm;

    private final AdminPermissionEvaluator auth;

    private final AdminEventBuilder adminEvent;

    protected final ClientConnection clientConnection;

    protected final KeycloakSession session;

    protected final HttpHeaders headers;

    public AdminRealmResourceProviderUsernameGeneratorResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent.resource(ResourceType.USER);
        this.headers = session.getContext().getRequestHeaders();
    }


	//@Override
	public Object getResource() {
		return this;
	}

	//@Override
	public void close() {
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello() {
		// do the authorization with the existing admin permissions (e.g. realm management roles)
		final UserPermissionEvaluator userPermissionEvaluator = auth.users();
		userPermissionEvaluator.requireQuery();

		Map<String,String> res=new HashMap<String,String>();
		res.put("hello","extension is active");

		return Response.ok(res).build();
	}



    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation( summary = "Create a new user Username is generated and unique")
    public Response createUser(final UserRepresentation rep) {
        //logger.info("Start of post request");
        // first check if user has manage rights
        try {
            auth.users().requireManage();
        } catch (ForbiddenException exception) {
            if (!canCreateGroupMembers(rep)) {
                throw exception;
            }
        }

        String username = rep.getUsername();
	String firstname= rep.getFirstName();
        String lastname= rep.getLastName();
	if (ObjectUtil.isBlank(firstname) || ObjectUtil.isBlank(lastname)) {
            logger.warn("Could not create user - firstname or lastname is blank");
            throw ErrorResponse.error("Could not create user - firstname or lastname is blank", Response.Status.BAD_REQUEST);

	}

        if(realm.isRegistrationEmailAsUsername()) {
            logger.warn("Could not create user - isRegistrationEmailAsUsername is set");
            throw ErrorResponse.error("Could not create user - isRegistrationEmailAsUsername ist set", Response.Status.BAD_REQUEST);
        }
        if (!ObjectUtil.isBlank(username)) {
            logger.warn("User name is given, although it should be generated");
            throw ErrorResponse.error("User name is given, although it should be generated", Response.Status.BAD_REQUEST);
        }

        // Double-check duplicated username and email here due to federation
//        if (session.users().getUserByUsername(realm, username) != null) {
//            throw ErrorResponse.exists("User exists with same username");
//        }

        username=UsernameGenerator.generate_username(session,realm,null,firstname,lastname,true);
	rep.setUsername(username);

        if (rep.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            try {
                if(session.users().getUserByEmail(realm, rep.getEmail()) != null) {
                    throw ErrorResponse.exists("User exists with same email");
                }
            } catch (ModelDuplicateException e) {
                throw ErrorResponse.exists("User exists with same email");
            }
        }

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);

        UserProfile profile = profileProvider.create(USER_API, rep.toAttributes());
       try {
            Response response = UserResource.validateUserProfile(profile, session, auth.adminAuth());
            if (response != null) {
                return response;
            }

            UserModel user = profile.create();

            UserResource.updateUserFromRep(profile, user, rep, session, false);
            RepresentationToModel.createFederatedIdentities(rep, session, realm, user);
            RepresentationToModel.createGroups(session, rep, realm, user);

            RepresentationToModel.createCredentials(rep, session, realm, user, true);
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), user.getId()).representation(rep).success();

            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().commit();
            }

            return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(user.getId()).build()).build();
        } catch (ModelDuplicateException e) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            throw ErrorResponse.exists("User exists with same username or email");
        } catch (PasswordPolicyNotMetException e) {
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            throw ErrorResponse.error("Password policy not met", Response.Status.BAD_REQUEST);
        } catch (ModelException me){
            if (session.getTransactionManager().isActive()) {
                session.getTransactionManager().setRollbackOnly();
            }
            logger.warn("Could not create user", me);
            throw ErrorResponse.error("Could not create user", Response.Status.BAD_REQUEST);
        }
    }


    private boolean canCreateGroupMembers(UserRepresentation rep) {
        if (!Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ)) {
            return false;
        }

        List<GroupModel> groups = Optional.ofNullable(rep.getGroups())
                .orElse(Collections.emptyList())
                .stream().map(path -> findGroupByPath(session, realm, path))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (groups.isEmpty()) {
            return false;
        }

        // if groups is part of the user rep, check if admin has manage_members and manage_membership on each group
        // an exception is thrown in case the current user does not have permissions to manage any of the groups
        for (GroupModel group : groups) {
            auth.groups().requireManageMembers(group);
            auth.groups().requireManageMembership(group);
        }

        return true;
    }


}
