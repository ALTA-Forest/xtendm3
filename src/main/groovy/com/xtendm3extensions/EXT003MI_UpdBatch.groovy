// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-08-10
// @version   1.0 
//
// Description 
// This API is to update a batch record in EXTDBH
// Transaction UpdBatch
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DBNO - Batch Number
 * @param: DBTP - Batch Type
 * @param: DBBU - Business Unit
 * @param: BUNA - Business Unit Name
 * @param: BDEL - Deliveries
 * @param: BTOT - Total
 * @param: STAT - Status
*/


public class UpdBatch extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDBNO
  int inDBTP
  String inDBBU
  String inBUNA
  String inNOTE
  int inBDEL
  double inBTOT
  int inSTAT
  
  // Constructor 
  public UpdBatch(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Batch Type
     if (mi.in.get("DBTP") != null) {
        inDBTP = mi.in.get("DBTP") 
     } 
           
     // Business Unit
     if (mi.in.get("DBBU") != null && mi.in.get("DBBU") != "") {
        inDBBU = mi.inData.get("DBBU").trim()
     } else {
        inDBBU = ""        
     }
 
     // Business Unit Name
     if (mi.in.get("BUNA") != null && mi.in.get("BUNA") != "") {
        inBUNA = mi.inData.get("BUNA").trim() 
     } else {
        inBUNA = ""        
     }

     // Deliveries
     if (mi.in.get("BDEL") != null) {
        inBDEL = mi.in.get("BDEL") 
     } 

     // Total
     if (mi.in.get("BTOT") != null) {
        inBTOT = mi.in.get("BTOT") 
     } 

     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } 

     // Note
     if (mi.in.get("NOTE") != null && mi.in.get("NOTE") != "") {
        inNOTE = mi.inData.get("NOTE").trim() 
     } else {
        inNOTE = ""        
     }


     // Validate Batch record
     Optional<DBContainer> EXTDBH = findEXTDBH(inCONO, inDIVI, inDBNO)
     if (!EXTDBH.isPresent()) {
        mi.error("Batch record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDBHRecord()
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
  // Update EXTDBH record
  //********************************************************************    
  void updEXTDBHRecord(){      
     DBAction action = database.table("EXTDBH").index("00").build()
     DBContainer EXTDBH = action.getContainer()     
     EXTDBH.set("EXCONO", inCONO)
     EXTDBH.set("EXDIVI", inDIVI)
     EXTDBH.set("EXDBNO", inDBNO)

     // Read with lock
     action.readLock(EXTDBH, updateCallBackEXTDBH)
     }
   
     Closure<?> updateCallBackEXTDBH = { LockedResult lockedResult ->      
     if (mi.in.get("DBTP") != null) {
        lockedResult.set("EXDBTP", mi.in.get("DBTP"))
     }

     if (mi.in.get("DBBU") != null) {
        lockedResult.set("EXDBBU", mi.in.get("DBBU"))
     }

     if (mi.in.get("BUNA") != null) {
        lockedResult.set("EXBUNA", mi.in.get("BUNA"))
     }

     if (mi.in.get("BDEL") != null) {
        lockedResult.set("EXBDEL", mi.in.get("BDEL"))
     }

     if (mi.in.get("BTOT") != null) {
        lockedResult.set("EXBTOT", mi.in.get("BTOT"))
     }
     
     if (mi.in.get("STAT") != null) {
        lockedResult.set("EXSTAT", mi.in.get("STAT"))
     }
   
     if (mi.in.get("NOTE") != null) {
        lockedResult.set("EXNOTE", mi.in.get("NOTE"))
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

