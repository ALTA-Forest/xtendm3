// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete a contract status from EXTDLH
// Transaction DelDelivery
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * 
*/


 public class DelDelivery extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final UtilityAPI utility
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
    int inDLNO      
  
  // Constructor 
  public DelDelivery(MIAPI mi, DatabaseAPI database, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database 
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0     
     }

     // Validate Delivery Header record
     Optional<DBContainer> EXTDLH = findEXTDLH(inCONO, inDIVI, inDLNO)
     if(!EXTDLH.isPresent()){
        mi.error("Delivery doesn't exist")   
        return             
     } else {
        // Update to status 99 instead of deleting the record
        updEXTDLHRecord()
     } 
     
  }


  //******************************************************************** 
  // Get EXTDLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLH(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLH").index("00").selectAllFields().build()
     DBContainer EXTDLH = query.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", DLNO)
     if(query.read(EXTDLH))  { 
       return Optional.of(EXTDLH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDLH
  //******************************************************************** 
  void deleteEXTDLHRecord(int CONO, String DIVI, int DLNO){ 
     DBAction action = database.table("EXTDLH").index("00").selectAllFields().build()
     DBContainer EXTDLH = action.getContainer()
     EXTDLH.set("EXCONO", CONO)
     EXTDLH.set("EXDIVI", DIVI)
     EXTDLH.set("EXDLNO", DLNO)

     action.readLock(EXTDLH, deleterCallbackEXTDLH)
  }
    
  Closure<?> deleterCallbackEXTDLH = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  
  
  //******************************************************************** 
  // Update EXTDLH record
  //********************************************************************    
  void updEXTDLHRecord(){      
     DBAction action = database.table("EXTDLH").index("00").build()
     DBContainer EXTDLH = action.getContainer()
     EXTDLH.set("EXCONO", inCONO)
     EXTDLH.set("EXDIVI", inDIVI)
     EXTDLH.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDLH, updateCallBackEXTDLH)
     }
   
     Closure<?> updateCallBackEXTDLH = { LockedResult lockedResult -> 
       lockedResult.set("EXSTAT", 99)
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)  
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

 }