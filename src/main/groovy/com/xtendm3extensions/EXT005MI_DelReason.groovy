// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete reason from EXTIRP
// Transaction DelReason
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RPNA - Reason Name
 * 
*/


 public class DelReason extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelReason(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Reason ID
     int inRPID    
     if (mi.in.get("RPID") != null) {
        inRPID = mi.in.get("RPID") 
     } else {
        inRPID = 0 
     }


     // Validate deck type record
     Optional<DBContainer> EXTIRP = findEXTIRP(inCONO, inDIVI, inRPID)
     if(!EXTIRP.isPresent()){
        mi.error("Reason doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTIRPRecord(inCONO, inDIVI, inRPID) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTIRP record
  //******************************************************************** 
  private Optional<DBContainer> findEXTIRP(int CONO, String DIVI, int RPID){  
     DBAction query = database.table("EXTIRP").index("00").build()
     DBContainer EXTIRP = query.getContainer()
     EXTIRP.set("EXCONO", CONO)
     EXTIRP.set("EXDIVI", DIVI)
     EXTIRP.set("EXRPID", RPID)
     if(query.read(EXTIRP))  { 
       return Optional.of(EXTIRP)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTIRP
  //******************************************************************** 
  void deleteEXTIRPRecord(int CONO, String DIVI, int RPID){ 
     DBAction action = database.table("EXTIRP").index("00").build()
     DBContainer EXTIRP = action.getContainer()
     EXTIRP.set("EXCONO", CONO)
     EXTIRP.set("EXDIVI", DIVI)
     EXTIRP.set("EXRPID", RPID)

     action.readLock(EXTIRP, deleterCallbackEXTIRP)
  }
    
  Closure<?> deleterCallbackEXTIRP = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }