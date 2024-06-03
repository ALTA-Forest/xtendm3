// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to delete batch item record from EXTDBC
// Transaction DelBatchCharge
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: TREF - Reference
 * @param: ITNO - Item Number
 * @param: CASN - Payee Number
 * @param: CDSE - Cost Sequence Number
*/

 public class DelBatchCharge extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    private final MICallerAPI miCaller
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelBatchCharge(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger, MICallerAPI miCaller) {
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
     inDIVI = mi.in.get("DIVI")
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

     // Reference
     String inTREF
     if (mi.in.get("TREF") != null && mi.in.get("TREF") != "") {
        inTREF = mi.inData.get("TREF").trim() 
     } else {
        inTREF = ""     
     }

     // Item Number
     String inITNO   
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } else {
        inITNO = ""     
     }

     // Payee number
     String inCASN   
     if (mi.in.get("CASN") != null && mi.in.get("CASN") != "") {
        inCASN = mi.inData.get("CASN").trim() 
     } else {
        inCASN = ""     
     }

     // Cost Sequence number
     String inCDSE   
     if (mi.in.get("CDSE") != null && mi.in.get("CDSE") != "") {
        inCDSE = mi.inData.get("CDSE").trim() 
     } else {
        inCDSE = ""     
     }


     // Validate batch charge record
     Optional<DBContainer> EXTDBC = findEXTDBC(inCONO, inDIVI, inDBNO, inTREF, inITNO, inCASN, inCDSE)
     if(!EXTDBC.isPresent()){
        mi.error("Batch Charge record doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTDBCRecord(inCONO, inDIVI, inDBNO, inTREF, inITNO, inCASN, inCDSE) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTDBC record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDBC(int CONO, String DIVI, int DBNO, String TREF, String ITNO, String CASN, String CDSE){  
     DBAction query = database.table("EXTDBC").index("00").build()
     DBContainer EXTDBC = query.getContainer()
     EXTDBC.set("EXCONO", CONO)
     EXTDBC.set("EXDIVI", DIVI)
     EXTDBC.set("EXDBNO", DBNO)
     EXTDBC.set("EXTREF", TREF)
     EXTDBC.set("EXITNO", ITNO)
     EXTDBC.set("EXCASN", CASN)
     EXTDBC.set("EXCDSE", CDSE)
     if(query.read(EXTDBC))  { 
       return Optional.of(EXTDBC)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDBC
  //******************************************************************** 
  void deleteEXTDBCRecord(int CONO, String DIVI, int DBNO, String TREF, String ITNO, String CASN, String CDSE){ 
     DBAction action = database.table("EXTDBC").index("00").build()
     DBContainer EXTDBC = action.getContainer()
     EXTDBC.set("EXCONO", CONO)
     EXTDBC.set("EXDIVI", DIVI)
     EXTDBC.set("EXDBNO", DBNO)
     EXTDBC.set("EXTREF", TREF)
     EXTDBC.set("EXITNO", ITNO)
     EXTDBC.set("EXCASN", CASN)
     EXTDBC.set("EXCDSE", CDSE)
     action.readLock(EXTDBC, deleterCallbackEXTDBC)
  }
    
  Closure<?> deleterCallbackEXTDBC = { LockedResult lockedResult ->  
     lockedResult.delete()
  }

 }