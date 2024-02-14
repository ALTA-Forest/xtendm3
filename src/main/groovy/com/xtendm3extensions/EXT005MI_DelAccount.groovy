// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete account from EXTACT
// Transaction DelAccount
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: ACCD - Account Code
 * 
*/


 public class DelAccount extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelAccount(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
     this.program = program
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
     String inACCD     
     if (mi.in.get("ACCD") != null) {
        inACCD = mi.in.get("ACCD") 
     } else {
        inACCD = "" 
     }


     // Validate account record
     Optional<DBContainer> EXTACT = findEXTACT(inCONO, inDIVI, inACCD)
     if(!EXTACT.isPresent()){
        mi.error("Account doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTACTRecord(inCONO, inDIVI, inACCD) 
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
  // Delete record from EXTACT
  //******************************************************************** 
  void deleteEXTACTRecord(int CONO, String DIVI, String ACCD){ 
     DBAction action = database.table("EXTACT").index("00").build()
     DBContainer EXTACT = action.getContainer()
     EXTACT.set("EXCONO", CONO)
     EXTACT.set("EXDIVI", DIVI)
     EXTACT.set("EXACCD", ACCD)

     action.readLock(EXTACT, deleterCallbackEXTACT)
  }
    
  Closure<?> deleterCallbackEXTACT = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }