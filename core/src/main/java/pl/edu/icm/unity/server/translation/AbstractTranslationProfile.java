/*
 * Copyright (c) 2013 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unity.server.translation;

import java.util.ArrayList;
import java.util.List;

import pl.edu.icm.unity.exceptions.InternalException;
import pl.edu.icm.unity.server.registries.TranslationActionsRegistry;
import pl.edu.icm.unity.types.DescribedObjectImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Common code for concrete translation profiles. Includes methods to implements JSON (de)serialization 
 * in the first place. 
 * @author K. Benedyczak
 */
public abstract class AbstractTranslationProfile<T extends AbstractTranslationRule<?>> extends DescribedObjectImpl 
	implements TranslationProfile
{
	protected List<T> rules;
	private ProfileType profileType;

	public AbstractTranslationProfile(String name, ProfileType profileType, List<T> rules)
	{
		this.rules = rules;
		this.profileType = profileType;
		setName(name);
	}

	public AbstractTranslationProfile()
	{
	}

	@Override
	public List<T> getRules()
	{
		return rules;
	}

	public void setRules(List<T> rules)
	{
		this.rules = rules;
	}

	public ProfileType getProfileType()
	{
		return profileType;
	}

	/**
	 * Common data for all profiles
	 * @param root
	 */
	protected void storePreable(ObjectNode root)
	{
		root.put("ver", "2");
		root.put("name", getName());
		root.put("description", getDescription());
		root.put("type", getProfileType().toString());		
	}

	protected void loadPreamble(ObjectNode root)
	{
		setName(root.get("name").asText());
		setDescription(root.get("description").asText());
		if (root.has("type"))
		{
			profileType = ProfileType.valueOf(root.get("type").asText());
		} else
			profileType = ProfileType.INPUT;
	}
	
	/**
	 * Stores array of rules in JSON
	 * @param root
	 */
	protected void storeRules(ObjectNode root)
	{
		ArrayNode jsonRules = root.putArray("rules");
		for (AbstractTranslationRule<?> rule: rules)
		{
			addAction(jsonRules, rule.getAction().getActionDescription().getName(),
					rule.getCondition().getCondition(), 
					rule.getAction().getParameters());
		}
	}
	
	@Override
	public String toJson(ObjectMapper jsonMapper)
	{
		try
		{
			return jsonMapper.writeValueAsString(toJsonObject(jsonMapper));
		} catch (JsonProcessingException e)
		{
			throw new InternalException("Can't serialize translation profile to JSON", e);
		}
	}
	
	public ObjectNode toJsonObject(ObjectMapper jsonMapper)
	{
		ObjectNode root = jsonMapper.createObjectNode();
		storePreable(root);
		storeRules(root);
		return root;
	}
	
	protected void fromJson(ObjectNode root, TranslationActionsRegistry registry)
	{
		try
		{
			loadPreamble(root);
			ArrayNode rulesA = (ArrayNode) root.get("rules");
			rules = new ArrayList<>(rulesA.size());
			for (int i=0; i<rulesA.size(); i++)
			{
				ObjectNode jsonRule = (ObjectNode) rulesA.get(i);
				String condition = jsonRule.get("condition").get("conditionValue").asText();
				ObjectNode jsonAction = (ObjectNode) jsonRule.get("action");
				String actionName = jsonAction.get("name").asText();
				TranslationActionFactory fact = registry.getByName(actionName);
				String[] parameters = extractParams(jsonAction);
				TranslationAction action = fact.getInstance(parameters);
				rules.add(createRule(action, new TranslationCondition(condition)));
			}
		} catch (Exception e)
		{
			throw new InternalException("Can't deserialize translation profile from JSON", e);
		}
	}
	
	protected void fromJson(String json, ObjectMapper jsonMapper, TranslationActionsRegistry registry)
	{
		try
		{
			ObjectNode root = (ObjectNode) jsonMapper.readTree(json);
			fromJson(root, registry);
		} catch (Exception e)
		{
			throw new InternalException("Can't deserialize translation profile from JSON", e);
		}
	}
	
	/**
	 * Must return a correct instance of a rule. Should check if the action is of proper type.
	 * @param action
	 * @param condition
	 * @return
	 */
	protected abstract T createRule(TranslationAction action, TranslationCondition condition);
	
	
	/**
	 * Add a profile's action to JSON array.
	 * @param jsonRules
	 * @param name
	 * @param condition
	 * @param args
	 */
	public static void addAction(ArrayNode jsonRules, String name, String condition, String... args)
	{
		ObjectNode jsonRule = jsonRules.addObject();
		ObjectNode jsonCondition = jsonRule.putObject("condition");
		jsonCondition.put("conditionValue", condition);
		ObjectNode jsonAction = jsonRule.putObject("action");
		jsonAction.put("name", name);
		ArrayNode jsonAParams = jsonAction.putArray("parameters");
		for (String param: args)
			jsonAParams.add(param);
	}
	
	/**
	 * 
	 * @param jsonAction
	 * @return array of action parameter values
	 */
	public static String[] extractParams(ObjectNode jsonAction)
	{
		ArrayNode jsonAParams = (ArrayNode) jsonAction.get("parameters");
		String[] parameters = new String[jsonAParams.size()];
		for (int j=0; j<jsonAParams.size(); j++)
			parameters[j] = jsonAParams.get(j).isNull() ? null : jsonAParams.get(j).asText();
		return parameters;
	}
}
