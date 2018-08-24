/*
 *-------------------
 * The Dictionary.java is part of ASH Viewer
 *-------------------
 * 
 * ASH Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ASH Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ASH Viewer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2009, Alex Kardapolov, All rights reserved.
 *
 */
package org.ash.util;

import java.util.*;

/**
 * The Class Dictionary.
 */
public class Dictionary extends ListResourceBundle {

  /* (non-Javadoc)
   * @see java.util.ListResourceBundle#getContents()
   */
  @Override
public Object[][] getContents() {
    return contents;
  }

  /** The Constant contents. */
  static final Object[][] contents = {
    {"error", "Error"},
    {"file.text", "File"},
    {"exit.text", "Exit"},
    {"settings.text", "Settings"},
    {"help.text", "Help"},
    {"about.text", "About"},
    {"file.mnemonic", "F"},
    {"exit.mnemonic", "X"},
    {"help.mnemonic", "H"},
    {"about.mnemonic", "A"},
    {"settings.mnemonic", "S"},
    {"database.text", "Database"},
    {"view.text", "View"},
    {"window.text", "Window"},
    {"trace.text", "Trace Session"},
    {"trace.mnemonic", "T"},
    {"quit application?","Quit application?"},
    {"delete all data?","Delete all data?"},
    {"delete data?","Delete data for selected time period?"},
    {"cant delete all data","You can't delete all data on active profile."},
    {"attention","Attention"},
    {"close all windows","Close all windows"},
    {"error on loading connections profile files.","Error on loading connections profile files."},
    {"deleteconn.text", "Delete Connection"},
    {"deleteconn.mnemonic", "D"},
    {"offlinemode.text", "Off-line mode"},
    {"offlinemode.mnemonic", "F"},
    {"newconn.text", "New Connection"},
    {"newconn.mnemonic", "N"},
    {"editconn.text", "Edit Connection"},
    {"editconn.mnemonic", "E"},
    {"closebutton.text", "Close"},
    {"closebutton.mnemonic", "C"},
    {"okbutton.text", "Ok"},
    {"okbutton.mnemonic", "O"},
    {"cancelbutton.text", "Cancel"},
    {"cancelbutton.mnemonic", "C"},
    {"copyconn.text", "Copy Connection"},
    {"copyconn.mnemonic", "P"},
    {"connections", "Connections"},
    {"error on saving connections profile files.","Error on saving connections profile files."},
    {"oracle database", "Oracle Database"},
    {"database connection", "Database Connection"},
    {"username", "Username"},
    {"password", "Password"},
    {"host", "Host"},
    {"port", "Port"},
    {"edition", "Edition"},
    {"connection name", "Connection Name"},
    {"auto commit", "Auto Commit"},
    {"transaction isolation level", "Transaction Isolation Level"},
    {"jdbc driver name", "JDBC Driver Name"},
    {"connection url", "Connection URL"},
    {"you must specify a connection name.","You must specify a connection name."},
    {"you must specify a connection url.","You must specify a connection url."},
    {"you must specify a connection username.","You must specify a connection username."},
    {"error when creating connection", "Error when creating connection"},
    {"error during tables fetching","Error during tables fetching"},
    {"error during catalogs fetching","Error during schemas fetching"},
    {"column","Column"},
    {"data type","Data Type"},
    {"null?","Null?"},
    {"error while fetching columns info","Error while fetching columns info"},
    {"error while executing query","Error while executing query"},
    {"error while executing statement","Error while executing statement"},
    {"error while loading profile","Error while loading profile"},
    {"error while saving profile","Error while saving profile"},
    {"error while loading old queries profile","Error while loading old queries profile"},
    {"error while saving old queries profile","error while saving old queries profile"},
    {"libs","Used libraries"},
    {"version","Version"},
    {"author","Author"},
    {"about","About"},
    {"release","Release"},
    {"error on closing connection.","Error on closing connection."},
    {"closing window?","Close this window?"},
    {"close win","Close internal windows"},
    {"error on commit connection.","Error on close connection."},
    {"settings", "Settings"},
    {"date format", "Date Format"},
    {"oracle plan table name", "Oracle Plan Table name"},
    {"ash login", "ASH Viewer login"},    
    {"language", "Language"},
    {"enable update when no pk found", "Enable update when no PK found"},
    {"date format not specified.", "Date Format not specified."},
    {"invalid date format.\nexample", "Invalid Date Format.\nExample"},
    {"explain plan table not specified.", "Explain Plan Table not specified."},
    {"error while saving", "Error while saving"},
    {"sql editor", "SQL Editor"},
    {"execute shell content (or selected content)", "Execute shell content (or selected content)"},
    {"explain plan", "Explain Plan"},
    {"old sql statements", "Old SQL statements"},
    {"records processed.", "records processed."},
    {"query execution", "Query Execution"},
    {"feature not supported for this database type.","Feature not supported for this database type."},
    {"record count","Record Count"},
    {"data export","Data Export"},
    {"selection","Sel"},
    {"export options","Export Options"},
    {"exported columns","Exported Columns"},
    {"destination","Destination"},
    {"to clipboard","To Clipboard"},
    {"to file","To File"},
    {"filename","Filename"},
    {"selectall.mnemonic","S"},
    {"selectall.text","Select All"},
    {"deselectall.mnemonic","D"},
    {"deselectall.text","Deselect All"},
    {"export format","Export Format"},
    {"columns","Columns"},
    {"excel (xls)","Excel (XLS)"},
    {"txt with delim","TXT with delim"},
    {"SQL (insert)","SQL (insert)"},
    {"export rows","Export Rows"},
    {"please select filename to store exporting rows.","Please select filename to store exporting rows."},
    {"process completed.","Process completed."},
    {"rows were exported.","rows were exported."},
    {"error while exporting data","Error while exporting data"},
    {"save data","Save Data"},
    {"please define a field delimiter.","Please define a field delimiter."},
    {"schemamemory.tooltip","Java garbage collector monitor (local)"},
    {"tables list","Tables list"},
    {"views list","Views list"},
    {"synonyms list","Synonyms list"},
    {"catalog","Schema"},
    {"tables","Tables"},
    {"views","Views"},
    {"synonyms","Synonyms"},
    {"trace sessions","Trace Sessions"},
    {"filter","Filter"},
    {"like","Like"},
    {"refresh (secs)","Refresh (secs)"},
    {"auto refresh data","Auto Refresh Data"},
    {"refresh","Refresh"},
    {"variable","Variable"},
    {"text","Text"},
    {"numeric","Numeric"},
    {"date","Date"},
    {"type","Type"},
    {"value","Value"},
    {"value is required","Value is required"},
    {"format sql on one row","Format SQL on one row"},
    {"format sql on more rows","Format SQL on more rows"},
    {"refresh all","Refresh All"},
    {"refresh detail","Refresh Detail"},
    {"table sort/filter","Table Sort/Filter"},
    {"clear sort","Clear Sort"},
    {"clear filter","Clear Filter"},
    {"order","Order"},
    {"filter","Filter"},
    {"move up","Move Up"},
    {"move down","Move Down"},
    {"move to left list","Move to left list"},
    {"sort by ascending order","Sort by ascending order"},
    {"sort by descending order","Sort by descending order"},
    {"copy row","Copy Row"},
    {"record count","Record Count"},
    {"data export...","Data Export..."},
    {"import file into...","Import File into..."},
    {"export to file...","Export to File..."},
    {"import file","Import File"},
    {"error while setting blob content","Error while setting blob content"},
    {"import file into blob field","Import file into blob field"},
    {"export to file","Export to File"},
    {"error while getting blob content","Error while getting blob content"},
    {"import file into blob field","Import file into BLOB field"},
    {"error while copying record","Error while copying record"},
    {"records found.","records found."},
    {"error while record counting","Error while record counting"},
    {"error on inserting data","Error on inserting data"},
    {"can't delete data: pk is not defined.","Can't delete data: Primary Key is not defined."},
    {"error on deleting data","Error on deleting data"},
    {"error on commit changes","Error on commit changes"},
    {"error on rollback changes","Error on rollback changes"},
    {"data","Data"},
    {"skip with chars","Skip with chars"},
    {"find","Find"},
    {"orderPanel","Order"},
    {"filterPanel","Filter"},
    {"work in progress...","Work in progress..."},
    {"script","Script"},
    {"script plugin","Script Plugin"},
    {"default","Default"},
    {"error while dropping table:","Error while dropping table:"},
    {"error while dropping column:","Error while dropping column:"},
    {"column name","Column Name"},
    {"column type","Column Type"},
    {"size","Size"},
    {"execute","Execute"},
    {"not null","Not Null"},
    {"importsql.text","Import SQL Script"},
    {"importsql.mnemonic","S"},
    {"import sql script","Import SQL script"},
    {"error while loading script file:","Error while loading script file:"},
    {"loading script file","Loading script file"},
    {"loading completed.","Loading completed."},
    {"table triggers plugin","Table triggers plugin"},
    {"triggers","Triggers"},
    {"filter by ","Filter By "},
    {"remove filter","Remove Filter"},
    {"data replication.text","Data Replication"},
    {"data replication profiles","Data Replication Profiles"},
    {"deleteprofile.text", "Delete Profile"},
    {"deleteprofile.mnemonic", "D"},
    {"newprofile.text", "New Profile"},
    {"newprofile.mnemonic", "N"},
    {"editprofile.text", "Edit Profile"},
    {"editprofile.mnemonic", "E"},
    {"profiles","Profiles"},
    {"execbutton.text", "Execute Replication"},
    {"execbutton.mnemonic", "R"},
    {"profile name","Profile Name"},
    {"source database","Source Database"},
    {"target database","Target Database"},
    {"source tables","Source Tables"},
    {"re-create tables content","Re-create tables content"},
    {"error when initializing window","Error when initializing window"},
    {"you cannot set the same connection for source and target database","You cannot set the same connection for source and target database"},
    {"you must set a profile name","You must set a profile name"},
    {"you must set a source and target database","You must set a source and target database"},
    {"you must select at least one table","You must select at least one table"},
    {"error on loading replication profile files.","Error on loading replication profile files."},
    {"error on loading replication profile file.","Error on loading replication profile file."},
    {"error on saving replication profile file.","Error on saving replication profile file."},
    {"database connections don't exist","Database connections don't exist"},
    {"error while replicating data","Error while replicating data"},
    {"data replication is completed.","Data replication is completed."},
    {"replication completed","Replication completed"},
    {"database schema","Database:"},
    {"schemabutton.tooltip","Open a new Database Schema"},
    {"dbschema.text","Database Schema"},
    {"dbschema.mnemonic","D"},
    {"printbutton.tooltip","Print Database Schema"},
    {"printfitbutton.tooltip","Print Database Schema and Fit to One Page"},
    {"error while printing schema","Error while printing schema"},
    {"please specify schema profile file name: ","Please specify schema profile file name: "},
    {"save database schema","Save Database Schema"},
    {"loadbutton.tooltip","Load Database Schema Profile File"},
    {"savebutton.tooltip","Save Database Schema Profile File"},
    {"expbutton.tooltip","Export Schema Diagram as PDF file"},
    {"the specified file already exists.\noverwrite it?","The specified file already exists.\nOverwrite it?"},
    {"save not allowed","Save not allowed"},
    {"error on saving","Error on saving"},
    {"load database schema profile file","Load database schema profile file"},
    {"profiles","Profiles"},
    {"error on loading","Error on loading"},
    {"error while exporting schema diagram","Error while exporting schema diagram"},
    {"add quotes","Add quotes to select/where fields"},
    {"runButtonSqlTab.tooltip", "Run query, Ctrl+Enter"},
    
    {"settingsMain.text","Settings"},
    {"ThumbnailMain.text","Thumbnail detail"},
    {"ThumbnailMain.mnemonic","T"},
    
    {"texttoclip.text","Copy to clipboard"},
    
    {"autoRadio.text","Auto"},
    {"manualRadio.text","Manual"},
    
    {"EERadio.text","Enterprise"},
    {"SERadio.text","Standard"},
    
    {"_0RadioButton.text","0"},
    {"_10RadioButton.text","10"},
    {"_30RadioButton.text","30"},
    {"_50RadioButton.text","50"},
    
    {"_sqlPlanTA.text","save to local DB"},
    {"_sqlPlanDetail.text","save to local DB"},
    
    {"scaleAutoRadioButton.text","Auto"},
    {"scaleX1Button.text","1xCPU_COUNT"},
    {"scaleX15Button.text","1.5xCPU_COUNT"},
    {"scaleX2Button.text","2xCPU_COUNT"},
    
    {"cpuLabel.text","CPU used"},
    {"schedulerLabel.text","Scheduler"},
    {"userIOLabel.text","User I/O"},
    {"systemIOLabel.text","System I/O"},
    {"concurrencyLabel.text","Concurrency"},
    {"applicationsLabel.text","Application"},
    {"commitLabel.text","Commit"},
    {"configurationLabel.text","Configuration"},    
    {"administrativeLabel.text","Administrative"},
    {"networkLabel.text","Network"},
    {"queueningLabel.text","Queueing"},
    {"clusterLabel.text","Cluster"},
    {"otherLabel.text","Other"},
    
    {"tabMain.text","Top activity"},
    {"tabDetail.text","Detail"},
    {"tabHistory.text","History"},
    
    {"tabTopSQL.text","Top SQL"},
    {"tabSQLText.text","SQL text"},
    {"tabSQLPlan.text","SQL plan"},
    
    /** audit_actions */
    {"0","UNKNOWN"},                                                                
    {"1","CREATE TABLE"},                                                           
    {"2","INSERT"},                                                                 
    {"3","SELECT"},                                                                 
    {"4","CREATE CLUSTER"},                                                         
    {"5","ALTER CLUSTER"},                                                          
    {"6","UPDATE"},                                                                 
    {"7","DELETE"},                                                                 
    {"8","DROP CLUSTER"},                                                           
    {"9","CREATE INDEX"},                                                           
    {"10","DROP INDEX"},                                                            
    {"11","ALTER INDEX"},                                                           
    {"12","DROP TABLE"},                                                            
    {"13","CREATE SEQUENCE"},                                                       
    {"14","ALTER SEQUENCE"},                                                        
    {"15","ALTER TABLE"},                                                           
    {"16","DROP SEQUENCE"},                                                         
    {"17","GRANT OBJECT"},                                                          
    {"18","REVOKE OBJECT"},                                                         
    {"19","CREATE SYNONYM"},                                                        
    {"20","DROP SYNONYM"},                                                          
    {"21","CREATE VIEW"},                                                           
    {"22","DROP VIEW"},                                                             
    {"23","VALIDATE INDEX"},                                                        
    {"24","CREATE PROCEDURE"},                                                      
    {"25","ALTER PROCEDURE"},                                                       
    {"26","LOCK"},                                                                  
    {"27","NO-OP"},                                                                 
    {"28","RENAME"},                                                                
    {"29","COMMENT"},                                                               
    {"30","AUDIT OBJECT"},                                                          
    {"31","NOAUDIT OBJECT"},                                                        
    {"32","CREATE DATABASE LINK"},                                                  
    {"33","DROP DATABASE LINK"},                                                    
    {"34","CREATE DATABASE"},                                                       
    {"35","ALTER DATABASE"},                                                        
    {"36","CREATE ROLLBACK SEG"},                                                   
    {"37","ALTER ROLLBACK SEG"},                                                    
    {"38","DROP ROLLBACK SEG"},                                                     
    {"39","CREATE TABLESPACE"},                                                     
    {"40","ALTER TABLESPACE"},                                                      
    {"41","DROP TABLESPACE"},                                                       
    {"42","ALTER SESSION"},                                                         
    {"43","ALTER USER"},                                                            
    {"44","COMMIT"},                                                                
    {"45","ROLLBACK"},                                                              
    {"46","SAVEPOINT"},                                                             
    {"47","PL/SQL EXECUTE"},                                                        
    {"48","SET TRANSACTION"},                                                       
    {"49","ALTER SYSTEM"},                                                          
    {"50","EXPLAIN"},                                                               
    {"51","CREATE USER"},                                                           
    {"52","CREATE ROLE"},                                                           
    {"53","DROP USER"},                                                             
    {"54","DROP ROLE"},                                                             
    {"55","SET ROLE"},                                                              
    {"56","CREATE SCHEMA"},                                                         
    {"57","CREATE CONTROL FILE"},                                                   
    {"59","CREATE TRIGGER"},                                                        
    {"60","ALTER TRIGGER"},                                                         
    {"61","DROP TRIGGER"},                                                          
    {"62","ANALYZE TABLE"},                                                         
    {"63","ANALYZE INDEX"},                                                         
    {"64","ANALYZE CLUSTER"},                                                       
    {"65","CREATE PROFILE"},                                                        
    {"66","DROP PROFILE"},                                                          
    {"67","ALTER PROFILE"},                                                         
    {"68","DROP PROCEDURE"},                                                        
    {"70","ALTER RESOURCE COST"},                                                   
    {"71","CREATE MATERIALIZED VIEW LOG"},                                          
    {"72","ALTER MATERIALIZED VIEW LOG"},                                           
    {"73","DROP MATERIALIZED VIEW LOG"},                                            
    {"74","CREATE MATERIALIZED VIEW"},                                              
    {"75","ALTER MATERIALIZED VIEW"},                                               
    {"76","DROP MATERIALIZED VIEW"},                                                
    {"77","CREATE TYPE"},                                                           
    {"78","DROP TYPE"},                                                             
    {"79","ALTER ROLE"},                                                            
    {"80","ALTER TYPE"},                                                            
    {"81","CREATE TYPE BODY"},                                                      
    {"82","ALTER TYPE BODY"},                                                       
    {"83","DROP TYPE BODY"},                                                        
    {"84","DROP LIBRARY"},                                                          
    {"85","TRUNCATE TABLE"},                                                        
    {"86","TRUNCATE CLUSTER"},                                                      
    {"91","CREATE FUNCTION"},                                                       
    {"92","ALTER FUNCTION"},                                                        
    {"93","DROP FUNCTION"},                                                         
    {"94","CREATE PACKAGE"},                                                        
    {"95","ALTER PACKAGE"},                                                         
    {"96","DROP PACKAGE"},                                                          
    {"97","CREATE PACKAGE BODY"},                                                   
    {"98","ALTER PACKAGE BODY"},                                                    
    {"99","DROP PACKAGE BODY"},                                                     
    {"100","LOGON"},                                                                
    {"101","LOGOFF"},                                                               
    {"102","LOGOFF BY CLEANUP"},                                                    
    {"103","SESSION REC"},                                                          
    {"104","SYSTEM AUDIT"},                                                         
    {"105","SYSTEM NOAUDIT"},                                                       
    {"106","AUDIT DEFAULT"},                                                        
    {"107","NOAUDIT DEFAULT"},                                                      
    {"108","SYSTEM GRANT"},                                                         
    {"109","SYSTEM REVOKE"},                                                        
    {"110","CREATE PUBLIC SYNONYM"},                                                
    {"111","DROP PUBLIC SYNONYM"},                                                  
    {"112","CREATE PUBLIC DATABASE LINK"},                                          
    {"113","DROP PUBLIC DATABASE LINK"},                                            
    {"114","GRANT ROLE"},                                                           
    {"115","REVOKE ROLE"},                                                          
    {"116","EXECUTE PROCEDURE"},                                                    
    {"117","USER COMMENT"},                                                         
    {"118","ENABLE TRIGGER"},                                                       
    {"119","DISABLE TRIGGER"},                                                      
    {"120","ENABLE ALL TRIGGERS"},                                                  
    {"121","DISABLE ALL TRIGGERS"},                                                 
    {"122","NETWORK ERROR"},                                                        
    {"123","EXECUTE TYPE"},                                                         
    {"128","FLASHBACK"},                                                            
    {"129","CREATE SESSION"},                                                       
    {"157","CREATE DIRECTORY"},                                                     
    {"158","DROP DIRECTORY"},                                                       
    {"159","CREATE LIBRARY"},                                                       
    {"160","CREATE JAVA"},                                                          
    {"161","ALTER JAVA"},                                                           
    {"162","DROP JAVA"},                                                            
    {"163","CREATE OPERATOR"},                                                      
    {"164","CREATE INDEXTYPE"},                                                     
    {"165","DROP INDEXTYPE"},                                                       
    {"167","DROP OPERATOR"},                                                        
    {"168","ASSOCIATE STATISTICS"},                                                 
    {"169","DISASSOCIATE STATISTICS"},                                              
    {"170","CALL METHOD"},                                                          
    {"171","CREATE SUMMARY"},                                                       
    {"172","ALTER SUMMARY"},                                                        
    {"173","DROP SUMMARY"},                                                         
    {"174","CREATE DIMENSION"},                                                     
    {"175","ALTER DIMENSION"},                                                      
    {"176","DROP DIMENSION"},                                                       
    {"177","CREATE CONTEXT"},                                                       
    {"178","DROP CONTEXT"},                                                         
    {"179","ALTER OUTLINE"},                                                        
    {"180","CREATE OUTLINE"},                                                       
    {"181","DROP OUTLINE"},                                                         
    {"182","UPDATE INDEXES"},                                                       
    {"183","ALTER OPERATOR"},  
    {"189","MERGE"},
    {"197","PURGE USER_RECYCLEBIN"},                                                
    {"198","PURGE DBA_RECYCLEBIN"},                                                 
    {"199","PURGE TABLESAPCE"},                                                     
    {"200","PURGE TABLE"},                                                          
    {"201","PURGE INDEX"},                                                          
    {"202","UNDROP OBJECT"},                                                        
    {"204","FLASHBACK DATABASE"},                                                   
    {"205","FLASHBACK TABLE"},                                                      
    {"206","CREATE RESTORE POINT"},                                                 
    {"207","DROP RESTORE POINT"},                                                   
    {"208","PROXY AUTHENTICATION ONLY"},                                            
    {"209","DECLARE REWRITE EQUIVALENCE"},                                          
    {"210","ALTER REWRITE EQUIVALENCE"},                                            
    {"211","DROP REWRITE EQUIVALENCE"}};
}