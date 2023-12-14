package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;

public class UsernameGenerator {

	public static String generate_username(KeycloakSession session, RealmModel realm, String prefix, String firstName, String lastName, boolean firstCharOfFirstNameOnly) {
		String userName;
		if (firstCharOfFirstNameOnly) {
			userName = firstName.substring(0,1)+"."+lastName;
		} else {
		        userName = firstName+"."+lastName;
		}

		if (prefix != null) {
			userName=prefix+userName;
		}

	        UserModel alreadyExistingUser = session.users().getUserByUsername(realm,userName);

        	if(alreadyExistingUser != null){
	           int index = 2;
        	   while (true){
               		String searchUsername = userName+index;
			alreadyExistingUser = session.users().getUserByUsername(realm, searchUsername);

	               if(alreadyExistingUser == null){
			   userName=searchUsername;
	                   break;
	               }
	               index++;
        	   }

	        }

		return userName;

        }

}
