/*
 * Copyright (c) 2007, 2008 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE file for licencing information.
 *
 * Created on Mar 17, 2007
 * Author: K. Benedyczak <golbi@mat.umk.pl>
 */

package pl.edu.icm.unity.store.rdbms;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pl.edu.icm.unity.base.utils.Log;
import pl.edu.icm.unity.exceptions.InternalException;
import pl.edu.icm.unity.store.StorageCleaner;
import pl.edu.icm.unity.store.StorageConfiguration;
import pl.edu.icm.unity.store.StorageEngine;
import pl.edu.icm.unity.store.StoreLoaderInternal;


/**
 * Provides general DB initialization, which is run at each startup. 
 * If needed activates creation of DB schema using {@link InitDB}.
 * Provides DB limits and checks if the schema version is correct.
 * 
 * @author K. Benedyczak
 */
@Component(DB.NAME)
public class DB implements StoreLoaderInternal
{
	private static final Logger log = Log.getLogger(Log.U_SERVER_DB, DB.class);
	public static final String DB_VERSION = "2_2_0";
	
	public static final String NAME = StorageCleaner.BEAN_PFX + "rdbms";

	private InitDB initDB;

	private DBSessionManager sessionMan;

	@Autowired
	public DB(DBSessionManager sessionMan, InitDB initDB, StorageConfiguration cfg) 
			throws Exception
	{
		this.sessionMan = sessionMan;
		this.initDB = initDB;
		if (cfg.getEngine() == StorageEngine.rdbms || cfg.getEngine() == StorageEngine.hz)
			initialize();
	}
	
	private final void verifyDBVersion(DBSessionManager sessionMan) throws InternalException
	{
		String actualDbVersion;
		try
		{
			actualDbVersion = checkCurrentVersion(sessionMan);
		} catch (Exception e)
		{
			throw new InternalException("Can't read version of the database. " +
				"Have you initialized it? Are connection details correctly " +
				"entered in configuration? The error was:\n\n" + e, e);
		}
		if (!actualDbVersion.equals(DB.DB_VERSION))
			throw new InternalException("The database is initialized with " +
				"wrong schema. It is of version: " + actualDbVersion + 
				" while you are using now version:" + DB.DB_VERSION);
	}
	
	public String checkCurrentVersion(DBSessionManager sessionMan) throws Exception
	{
		SqlSession sqlMap = sessionMan.getSqlSession(false);
		try
		{
			return sqlMap.selectOne("getDBVersion");
		} finally
		{
			sqlMap.close();
		}
	}
	
	public void initialize() throws Exception
	{
		log.info("Initializing RDBMS storage engine");
		initDB.initIfNeeded();
		verifyDBVersion(sessionMan);
	}

	@Override
	public void reset()
	{
		initDB.resetDatabase();
	}
}