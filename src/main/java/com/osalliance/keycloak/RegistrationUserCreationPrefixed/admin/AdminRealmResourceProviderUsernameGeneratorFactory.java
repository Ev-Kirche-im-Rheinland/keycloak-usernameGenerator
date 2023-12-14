package com.osalliance.keycloak.RegistrationUserCreationPrefixed.admin;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.common.Profile;
import org.keycloak.models.RealmModel;

@AutoService(AdminRealmResourceProviderFactory.class)
public class AdminRealmResourceProviderUsernameGeneratorFactory implements AdminRealmResourceProviderFactory, EnvironmentDependentProviderFactory, AdminRealmResourceProvider
{

	public static final String PROVIDER_ID = "username-generator-admin-restapi";

	@Override
	public AdminRealmResourceProvider create(KeycloakSession session) {
		return this;
	}

	@Override
	public Object getResource(KeycloakSession keycloakSession, RealmModel realmModel,AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
		return new AdminRealmResourceProviderUsernameGeneratorResource(keycloakSession,auth,adminEvent);
	}

	@Override
	public void init(Config.Scope scope) {
	}

	@Override
	public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
	}

	@Override
	public void close() {
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public boolean isSupported() {
		return Profile.isFeatureEnabled(Profile.Feature.ADMIN2);
	}
}
