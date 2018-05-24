/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.engine.api;

import java.util.Collection;

import pl.edu.icm.unity.exceptions.AuthorizationException;
import pl.edu.icm.unity.exceptions.EngineException;
import pl.edu.icm.unity.types.authn.AuthenticationFlowDefinition;

/**
 * API for authentication flow management.
 * 
 * @author P.Piernik
 *
 */
public interface AuthenticationFlowManagement
{
	/**
	 * Add new authentication flow
	 * @param authenticatorsFlowDefinition
	 * @throws AuthorizationException
	 */
	void addAuthenticationFlowDefinition(AuthenticationFlowDefinition authenticatorsFlowDefinition) throws EngineException;
	
	/**
	 * Removes an existing authentication flow. The authentication flow must not be used by any of the endpoints,
	 * to be removed.
	 * @param toRemove authentication flow id
	 * @throws AuthorizationException
	 */
	void removeAuthenticationFlowDefinition(String toRemove) throws EngineException;
	
	/**
	 * 
	 * @return list of currently configured authentication flow
	 * @throws EngineException 
	 */
	Collection<AuthenticationFlowDefinition> getAuthenticationFlows() throws EngineException;

	/**
	 * Update existing authentication flow
	 * @param authFlowdef
	 */
	void updateAuthenticationFlowDefinition(AuthenticationFlowDefinition authFlowdef) throws EngineException;
}
