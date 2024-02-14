// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update deck header in EXTDPD
// Transaction UpdDeckDet
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DPID - Deck ID
 * @param: LOAD - Current Loads
 * @param: DLOG - Current Logs
 * @param: GVBF - Gross Volume BF
 * @param: NVBF - Net Volume BF
 * @param: ESWT - Estimated Weight
 * @param: TCOI - Cost of Inventory
 * @param: AWBF - Average Weight of 1 Mbf
 * @param: GBFL - Average Gross bf/Log
 * @param: NBFL - Average Net bf/Log
*/



public class UpdDeckDet extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDPID
  double inLOAD
  double inDLOG
  double inGVBF
  double inNVBF
  double inESWT
  double inTCOI
  double inAWBF
  double inGBFL
  double inNBFL

  
  // Constructor 
  public UpdDeckDet(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Deck ID
     if (mi.in.get("DPID") != null) {
        inDPID = mi.in.get("DPID") 
     } else {
        inDPID = 0         
     }
           
     // Current Loads
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     }

     // Current Logs
     if (mi.in.get("DLOG") != null) {
        inDLOG = mi.in.get("DLOG") 
     } 

     // Gross Volume
     if (mi.in.get("GVBF") != null) {
        inGVBF = mi.in.get("GVBF") 
     } 
     
     // Net Volume
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } 

     // Estimated Weight
     if (mi.in.get("ESWT") != null) {
        inESWT = mi.in.get("ESWT") 
     } 

     // Cost of Inventory
     if (mi.in.get("TCOI") != null) {
        inTCOI = mi.in.get("TCOI") 
     } 

     // Average Weight of 1 Mbf
     if (mi.in.get("AWBF") != null) {
        inAWBF = mi.in.get("AWBF") 
     } 

     // Average Gross bf/Log
     if (mi.in.get("GBFL") != null) {
        inGBFL = mi.in.get("GBFL") 
     } 

     // Average Net bf/Log
     if (mi.in.get("NBFL") != null) {
        inNBFL = mi.in.get("NBFL") 
     } 


     // Validate Deck Detail record
     Optional<DBContainer> EXTDPD = findEXTDPD(inCONO, inDIVI, inDPID)
     if (!EXTDPD.isPresent()) {
        mi.error("Deck Details doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDPDRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDPD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPD(int CONO, String DIVI, int DPID){  
     DBAction query = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = query.getContainer()
     EXTDPD.set("EXCONO", CONO)
     EXTDPD.set("EXDIVI", DIVI)
     EXTDPD.set("EXDPID", DPID)
     if(query.read(EXTDPD))  { 
       return Optional.of(EXTDPD)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTDPD record
  //********************************************************************    
  void updEXTDPDRecord(){      
     DBAction action = database.table("EXTDPD").index("00").build()
     DBContainer EXTDPD = action.getContainer()     
     EXTDPD.set("EXCONO", inCONO)
     EXTDPD.set("EXDIVI", inDIVI)
     EXTDPD.set("EXDPID", inDPID)

     // Read with lock
     action.readLock(EXTDPD, updateCallBackEXTDPD)
     }
   
     Closure<?> updateCallBackEXTDPD = { LockedResult lockedResult -> 
       if (mi.in.get("LOAD") != null) {
          lockedResult.set("EXLOAD", inLOAD)
       }
  
       if (mi.in.get("DLOG") != null) {
          lockedResult.set("EXDLOG", inDLOG)
       }
   
       if (mi.in.get("GVBF") != null) {
          lockedResult.set("EXGVBF", inGVBF)
       }
      
       if (mi.in.get("NVBF") != null) {
          lockedResult.set("EXNVBF", inNVBF)
       }
  
       if (mi.in.get("ESWT") != null) {
          lockedResult.set("EXESWT", inESWT)
       }
  
       if (mi.in.get("TCOI") != null) {
          lockedResult.set("EXCOST", inTCOI)
       }
  
       if (mi.in.get("AWBF") != null) {
          lockedResult.set("EXAWBF", inAWBF)
       }
  
       if (mi.in.get("GBFL") != null) {
          lockedResult.set("EXGBFL", inGBFL)
       }
  
       if (mi.in.get("NBFL") != null) {
          lockedResult.set("EXNBFL", inNBFL)
       }
  
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changeddate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changeddate)        
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

