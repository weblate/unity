/*
 * Copyright (c) 2018 Bixbit - Krzysztof Benedyczak All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package pl.edu.icm.unity.store.migration.from2_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.store.export.Update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Update JSon dump from V7 version, see {@link UpdateHelperFrom2_8}
 * @author P.Piernik
 *
 */
@Component
public class JsonDumpUpdateFromV7 implements Update
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_DB, JsonDumpUpdateFromV7.class);
	
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public InputStream update(InputStream is) throws IOException
	{
		ObjectNode root = (ObjectNode) objectMapper.readTree(is);
		ObjectNode contents = (ObjectNode) root.get("contents");
	
		ObjectNode newContents = insertFiles(contents);
		addCertificate(newContents);
		root.set("contents", newContents);
		
		return new ByteArrayInputStream(objectMapper.writeValueAsBytes(root));
	
	}

	private ObjectNode insertFiles(ObjectNode contents)
	{
		ObjectNode newContents = new ObjectNode(JsonNodeFactory.instance);
		Iterator<Map.Entry<String, JsonNode>> fields = contents.fields();
		while(fields.hasNext()){
		    Map.Entry<String, JsonNode> entry = fields.next();
		    newContents.putPOJO(entry.getKey(), entry.getValue());
		    if("attributes".equals(entry.getKey())){
			    log.info("Add empty files array");
			    newContents.putArray("files");
		    }
		}
		return newContents;
	}
	
	private void addCertificate(ObjectNode contents)
	{	
		log.info("Add empty certificate array");
		contents.putArray("certificate");
	}
}