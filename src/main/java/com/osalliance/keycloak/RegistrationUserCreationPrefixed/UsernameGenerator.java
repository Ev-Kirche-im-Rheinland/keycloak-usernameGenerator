package com.osalliance.keycloak.RegistrationUserCreationPrefixed;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.RealmModel;
import org.keycloak.common.util.ObjectUtil;

public class UsernameGenerator {
	private static String replace_umlauts(String src) {
		if (ObjectUtil.isBlank(src)) return src;
		String target=src.replace("Ä","Ae");
		target=target.replace("ä","ae");
		target=target.replace("Ö","Oe");
		target=target.replace("ö","oe");
		target=target.replace("Ü","Ue");
		target=target.replace("ü","ue");
		target=target.replace("ß","ss");
		target=target.replace("š","s");
		target=target.replace("&amp"," ");
		target=target.replace("&"," ");
		return target.trim();
	}

	public static String generate_username(KeycloakSession session, RealmModel realm, String prefix, String firstName, String lastName, boolean firstCharOfFirstNameOnly) {
		String userName=null;
		
		firstName=replace_umlauts(firstName);
		lastName=replace_umlauts(lastName);
		if (!ObjectUtil.isBlank(firstName))
			firstName=firstName.split("\\s")[0];
		if (!ObjectUtil.isBlank(lastName))
			lastName=lastName.split("\\s")[0];

                if ( ObjectUtil.isBlank(firstName) && ObjectUtil.isBlank(lastName) )
                    userName="vorname.nachname";
                else
                    if (ObjectUtil.isBlank(lastName))
			userName=firstName;
                if (userName==null) {
  			if (firstCharOfFirstNameOnly) {                
				userName = firstName.substring(0,1)+"."+lastName;
			} else {
			        userName = firstName+"."+lastName;
			}
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
