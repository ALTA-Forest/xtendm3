// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to update a batch item record in EXTDBC
// Transaction UpdBatchCharge
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
 * @param: SUNM - Payee Name
 * @param: CDSE - Cost Sequence Number
 * @param: SUCM - Cost Element
 * @param: INBN - Invoice Batch Number
 * @param: INDT - Invoice Date
 * @param: INAM - Batch Amount
*/


public class UpdBatchCharge extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDBNO
  String inTREF
  String inITNO
  String inCASN
  String inSUNM
  String inCDSE
  String inSUCM
  int inINBN
  int inINDT
  double inINAM
  
  // Constructor 
  public UpdBatchCharge(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.logger = logger  
     this.utility = utility
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
     if (mi.in.get("DBNO") != null) {
        inDBNO = mi.in.get("DBNO") 
     } else {
        inDBNO = 0        
     }

     // Reference
     if (mi.in.get("TREF") != null && mi.in.get("TREF") != "") {
        inTREF = mi.inData.get("TREF").trim()
     } 
 
     // Item Number
     if (mi.in.get("ITNO") != null && mi.in.get("ITNO") != "") {
        inITNO = mi.inData.get("ITNO").trim() 
     } 

     // Payee Number
     if (mi.in.get("CASN") != null && mi.in.get("CASN") != "") {
        inCASN = mi.inData.get("CASN").trim() 
     } 

     // Payee Name
     if (mi.in.get("SUNM") != null && mi.in.get("SUNM") != "") {
        inSUNM = mi.inData.get("SUNM").trim() 
     }

     // Cost Sequence Number
     if (mi.in.get("CDSE") != null && mi.in.get("CDSE") != "") {
        inCDSE = mi.inData.get("CDSE").trim() 
     } 

     // Cost Element
     if (mi.in.get("SUCM") != null && mi.in.get("SUCM") != "") {
        inSUCM = mi.inData.get("SUCM").trim() 
     } 

     // Invoice Batch Number
     if (mi.in.get("INBN") != null) {
        inINBN = mi.in.get("INBN") 
     } 

     // Invoice Date
     if (mi.in.get("INDT") != null) {
        inINDT = mi.in.get("INDT") 
        
        //Validate date format
        boolean validINDT = utility.call("DateUtil", "isDateValid", String.valueOf(inINDT), "yyyyMMdd")  
        if (!validINDT) {
           mi.error("Invoice Date is not valid")   
           return  
        } 

     } 

     // Batch Amount
     if (mi.in.get("INAM") != null) {
        inINAM = mi.in.get("INAM") 
     } 

     // Validate Batch record
     Optional<DBContainer> EXTDBC = findEXTDBC(inCONO, inDIVI, inDBNO, inTREF, inITNO, inCASN, inCDSE)
     if (!EXTDBC.isPresent()) {
        mi.error("Batch record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDBCRecord()
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
  // Update EXTDBC record
  //********************************************************************    
  void updEXTDBCRecord(){      
     DBAction action = database.table("EXTDBC").index("00").build()
     DBContainer EXTDBC = action.getContainer()     
     EXTDBC.set("EXCONO", inCONO)
     EXTDBC.set("EXDIVI", inDIVI)
     EXTDBC.set("EXDBNO", inDBNO)
     EXTDBC.set("EXTREF", inTREF)
     EXTDBC.set("EXITNO", inITNO)
     EXTDBC.set("EXCASN", inCASN)
     EXTDBC.set("EXCDSE", inCDSE)
     
     // Read with lock
     action.readLock(EXTDBC, updateCallBackEXTDBC)
     }
   
     Closure<?> updateCallBackEXTDBC = { LockedResult lockedResult ->      
     if (mi.in.get("SUNM") != null) {
        lockedResult.set("EXSUNM", mi.in.get("SUNM"))
     }

     if (mi.in.get("SUCM") != null) {
        lockedResult.set("EXSUCM", mi.in.get("SUCM"))
     }

     if (mi.in.get("INBN") != null) {
        lockedResult.set("EXINBN", mi.in.get("INBN"))
     }

     if (mi.in.get("INDT") != null) {
        lockedResult.set("EXINDT", mi.in.get("INDT"))
     }

     if (mi.in.get("INAM") != null) {
        lockedResult.set("EXINAM", mi.in.get("INAM"))
     }
     
     int changeNo = lockedResult.get("EXCHNO")
     int newChangeNo = changeNo + 1 
     int changedate = utility.call("DateUtil", "currentDateY8AsInt")
     lockedResult.set("EXLMDT", changedate)        
     lockedResult.set("EXCHNO", newChangeNo) 
     lockedResult.set("EXCHID", program.getUser())
     lockedResult.update()
  }

} 

