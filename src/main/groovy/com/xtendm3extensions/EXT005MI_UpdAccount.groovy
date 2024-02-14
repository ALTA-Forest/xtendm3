// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update an account in EXTACT
// Transaction UpdAccount
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: ACCD - Account Code
 * @param: NAME - Name
 * 
*/



public class UpdAccount extends ExtendM3Transaction {
  private final MIAPI mi
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inACCD
  String inNAME
  
  // Constructor 
  public UpdAccount(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
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

     // Account Code
     if (mi.in.get("ACCD") != null) {
        inACCD = mi.in.get("ACCD") 
     } else {
        inACCD = ""         
     }
           
     // Name
     if (mi.in.get("NAME") != null) {
        inNAME = mi.in.get("NAME") 
     } else {
        inNAME= ""      
     }



     // Validate Account record
     Optional<DBContainer> EXTACT = findEXTACT(inCONO, inDIVI, inACCD)
     if (!EXTACT.isPresent()) {
        mi.error("Account doesn't exist")   
        return             
     } else {
        // Update record
        updEXTACTRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTACT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTACT(int CONO, String DIVI, String ACCD){  
     DBAction query = database.table("EXTACT").index("00").build()
     DBContainer EXTACT = query.getContainer()
     EXTACT.set("EXCONO", CONO)
     EXTACT.set("EXDIVI", DIVI)
     EXTACT.set("EXACCD", ACCD)
     if(query.read(EXTACT))  { 
       return Optional.of(EXTACT)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTACT record
  //********************************************************************    
  void updEXTACTRecord(){      
     DBAction action = database.table("EXTACT").index("00").build()
     DBContainer EXTACT = action.getContainer()     
     EXTACT.set("EXCONO", inCONO)
     EXTACT.set("EXDIVI", inDIVI)
     EXTACT.set("EXACCD", inACCD)

     // Read with lock
     action.readLock(EXTACT, updateCallBackEXTACT)
     }
   
     Closure<?> updateCallBackEXTACT = { LockedResult lockedResult -> 
       if (inNAME != null && inNAME != "") {
          lockedResult.set("EXNAME", inNAME)
       }
  
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)       
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

