// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to update a delivery volume record in EXTDTV
// Transaction UpdDeliveryVol
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: LOAD - Load
 * @param: VLOG - Logs
 * @param: GVBF - Gross Volume
 * @param: NVBF - Net Volume
*/


public class UpdDeliveryVol extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final LoggerAPI logger
  private final UtilityAPI utility
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inLOAD
  int inVLOG
  double inGVBF
  double inNVBF
  
  // Constructor 
  public UpdDeliveryVol(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, LoggerAPI logger, UtilityAPI utility) {
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

     // Delivery Number
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0        
     }

     // Load
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } 

     // Log
     if (mi.in.get("VLOG") != null) {
        inVLOG = mi.in.get("VLOG") 
     }

     // Gross Volume
     if (mi.in.get("GVBF") != null) {
        inGVBF = mi.in.get("GVBF") 
     } 

     // Net Volume
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } 

     // Validate Delivery Volume record
     Optional<DBContainer> EXTDTV = findEXTDTV(inCONO, inDIVI, inDLNO)
     if (!EXTDTV.isPresent()) {
        mi.error("Delivery Volume record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDTVRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDTV record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDTV(int CONO, String DIVI, int DLNO){  
     DBAction query = database.table("EXTDTV").index("00").build()
     def EXTDTV = query.getContainer()
     EXTDTV.set("EXCONO", CONO)
     EXTDTV.set("EXDIVI", DIVI)
     EXTDTV.set("EXDLNO", DLNO)
     if(query.read(EXTDTV))  { 
       return Optional.of(EXTDTV)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTDTV record
  //********************************************************************    
  void updEXTDTVRecord(){      
     DBAction action = database.table("EXTDTV").index("00").build()
     DBContainer EXTDTV = action.getContainer()     
     EXTDTV.set("EXCONO", inCONO)
     EXTDTV.set("EXDIVI", inDIVI)
     EXTDTV.set("EXDLNO", inDLNO)

     // Read with lock
     action.readLock(EXTDTV, updateCallBackEXTDTV)
     }
   
     Closure<?> updateCallBackEXTDTV = { LockedResult lockedResult ->      
       if (mi.in.get("LOAD") != null) {
          lockedResult.set("EXLOAD", mi.in.get("LOAD"))
       }
  
       if (mi.in.get("VLOG") != null) {
          lockedResult.set("EXVLOG", mi.in.get("VLOG"))
       }
    
       if (mi.in.get("GVBF") != null) {
          lockedResult.set("EXGVBF", mi.in.get("GVBF"))
       }
  
       if (mi.in.get("NVBF") != null) {
          lockedResult.set("EXNVBF", mi.in.get("NVBF"))
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

