// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete batch from EXTDBH
// Transaction DelBatch
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * 
*/

 public class DelBatch extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelBatch(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
     this.mi = mi
     this.database = database
     this.program = program
     this.logger = logger
     this.miCaller = miCaller
  } 
    
  public void main() { 
     // Set Company Number
     inCONO = mi.in.get("CONO")      
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
     inDIVI = mi.inData.get("DIVI").trim()
     if (inDIVI == null || inDIVI == "") {
        inDIVI = program.LDAZD.DIVI
     }

     // Batch Number
     int inDBNO   
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO") 
     } else {
        inDBNO = 0     
     }


     // Validate batch record
     Optional<DBContainer> EXTDBH = findEXTDBH(inCONO, inDIVI, inDBNO)
     if(!EXTDBH.isPresent()){
        mi.error("Batch Number doesn't exist")   
        return             
     } else {
        // Delete header record 
        deleteEXTDBHRecord(inCONO, inDIVI, inDBNO) 
        // Delete detail record
        deleteEXTDBDRecord(inCONO, inDIVI, inDBNO) 
        // Delete charge record
        deleteEXTDBCRecord(inCONO, inDIVI, inDBNO) 
        // Delete charge record
        deleteEXTDBIRecord(inCONO, inDIVI, inDBNO) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDBH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBH(int CONO, String DIVI, int DBNO){  
     DBAction query = database.table("EXTDBH").index("00").build()
     DBContainer EXTDBH = query.getContainer()
     EXTDBH.set("EXCONO", CONO)
     EXTDBH.set("EXDIVI", DIVI)
     EXTDBH.set("EXDBNO", DBNO)
     if(query.read(EXTDBH))  { 
       return Optional.of(EXTDBH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDBH
  //******************************************************************** 
  void deleteEXTDBHRecord(int CONO, String DIVI, int DBNO){ 
     DBAction action = database.table("EXTDBH").index("00").build()
     DBContainer EXTDBH = action.getContainer()
     EXTDBH.set("EXCONO", CONO)
     EXTDBH.set("EXDIVI", DIVI)
     EXTDBH.set("EXDBNO", DBNO)
     action.readLock(EXTDBH, deleterCallbackEXTDBH)
  }
    
  Closure<?> deleterCallbackEXTDBH = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record from EXTDBC
  //******************************************************************** 
  void deleteEXTDBCRecord(int CONO, String DIVI, int DBNO){ 
     DBAction action = database.table("EXTDBC").index("00").build()
     DBContainer EXTDBC = action.getContainer()
     EXTDBC.set("EXCONO", CONO)
     EXTDBC.set("EXDIVI", DIVI)
     EXTDBC.set("EXDBNO", DBNO)
     action.readAllLock(EXTDBC, 3, deleterCallbackEXTDBC)
  }
    
  Closure<?> deleterCallbackEXTDBC = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record from EXTDBD
  //******************************************************************** 
  void deleteEXTDBDRecord(int CONO, String DIVI, int DBNO){ 
     DBAction action = database.table("EXTDBD").index("00").build()
     DBContainer EXTDBD = action.getContainer()
     EXTDBD.set("EXCONO", CONO)
     EXTDBD.set("EXDIVI", DIVI)
     EXTDBD.set("EXDBNO", DBNO)
     action.readAllLock(EXTDBD, 3, deleterCallbackEXTDBD)
  }
    
  Closure<?> deleterCallbackEXTDBD = { LockedResult lockedResult ->  
     lockedResult.delete()
  }


  //******************************************************************** 
  // Delete record from EXTDBI
  //******************************************************************** 
  void deleteEXTDBIRecord(int CONO, String DIVI, int DBNO){ 
     DBAction action = database.table("EXTDBI").index("00").build()
     DBContainer EXTDBI = action.getContainer()
     EXTDBI.set("EXCONO", CONO)
     EXTDBI.set("EXDIVI", DIVI)
     EXTDBI.set("EXDBNO", DBNO)
     action.readAllLock(EXTDBI, 3, deleterCallbackEXTDBI)
  }
    
  Closure<?> deleterCallbackEXTDBI = { LockedResult lockedResult ->  
     lockedResult.delete()
  }

 }