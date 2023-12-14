package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.AdminEventBuilder;

@AutoService(RealmResourceProviderFactory.class)
public class RealmResourceProviderUsernameGeneratorFactory implements RealmResourceProviderFactory
{

	public static final String PROVIDER_ID = "username-generator-restapi";

	@Override
	public RealmResourceProvider create(KeycloakSession keycloakSession) {
		return new RealmResourceProviderUsernameGeneratorProvider(keycloakSession);
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

}
