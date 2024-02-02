// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update delivery details in EXTDLD
// Transaction UpdDeliveryDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: NOTE - Notes
 * @param: WTNO - Weight Ticket Number
 * @param: STNO - Scale Ticket Number
 * 
*/



public class UpdDeliveryDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  int inWTNO
  String inSTNO  
  
  // Constructor 
  public UpdDeliveryDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0         
     }
 
     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0         
     }
          
     // Weight Ticket Number
     if (mi.in.get("WTNO") != null) {
        inWTNO = mi.in.get("WTNO") 
     } else {
        inWTNO = 0        
     }
     
     // Scale Ticket Number
     if (mi.in.get("STNO") != null) {
        inSTNO = mi.in.get("STNO") 
     } else {
        inSTNO = ""        
     }


     // Validate Delivery Detail record
     Optional<DBContainer> EXTDLD = findEXTDLD(inCONO, inDIVI, inDLNO)
     if (!EXTDLD.isPresent()) {
        mi.error("Delivery details doesn't exists")   
        return             
     } else {    
        // Update record
        updEXTDLDRecord()
     }
     
  }
  
  //******************************************************************** 
  // Get EXTDLD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLD(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDLD").index("00").build()
     DBContainer EXTDLD = query.getContainer()
     EXTDLD.set("EXCONO", CONO)
     EXTDLD.set("EXDIVI", DIVI)
     EXTDLD.set("EXDLNO", DLNO)
     if(query.read(EXTDLD))  { 
       return Optional.of(EXTDLD)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTDLD record
  //********************************************************************    
  void updEXTDLDRecord(){      
     DBAction action = database.table("EXTDLD").index("00").build()
     DBContainer EXTDLD = action.getContainer()     
     EXTDLD.set("EXCONO", inCONO)
     EXTDLD.set("EXDIVI", inDIVI)
     EXTDLD.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDLD, updateCallBackEXTDLD)
     }
   
     Closure<?> updateCallBackEXTDLD = { LockedResult lockedResult -> 
       if (inCTNO != 0) {
          lockedResult.set("EXCTNO", inCTNO)
       }
  
       if (inWTNO != 0) {
          lockedResult.set("EXWTNO", inWTNO)
       }
  
       if (inSTNO != "") {
          lockedResult.set("EXSTNO", inSTNO)
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

