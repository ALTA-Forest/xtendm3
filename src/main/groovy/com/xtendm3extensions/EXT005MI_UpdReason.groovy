// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update reason in EXTIRP
// Transaction UpdReason
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RPNA - Reason Name
 * @param: RECD - Reason Code
 * @param: ISPC - Is Percentage
 * @param: LOAD - Loads
 * @param: DLOG - Logs
 * @param: GVBF - Gross Volume
 * @param: NVBF - Net Volume
 * @param: PCTG - Percentage
 * 
*/



public class UpdReason extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inRPID
  String inRPNA
  String inRECD
  int inISPC
  double inLOAD
  double inDLOG
  double inGVBF
  double inNVBF
  double inPCTG
  int inEINV
  String inNOTE
     
  
  // Constructor 
  public UpdReason(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Reason ID
     if (mi.in.get("RPID") != null) {
        inRPID = mi.in.get("RPID") 
     } else {
        inRPID = 0         
     }           

     // Reason Name
     if (mi.in.get("RPNA") != null) {
        inRPNA = mi.in.get("RPNA") 
     } else {
        inRPNA = ""         
     }
     
     // Reason Code
     if (mi.in.get("RECD") != null) {
        inRECD = mi.in.get("RECD") 
     } else {
        inRECD = ""         
     }

     // Is Percentage
     if (mi.in.get("ISPC") != null) {
        inISPC = mi.in.get("ISPC") 
     }           

     // Loads
     if (mi.in.get("LOAD") != null) {
        inLOAD = mi.in.get("LOAD") 
     } 

     // Logs
     if (mi.in.get("DLOG") != null) {
        inDLOG = mi.in.get("DLOG") 
     } 

     // Gross Volume BF
     if (mi.in.get("GVBF") != null) {
        inGVBF = mi.in.get("GVBF") 
     } 
     
     // Net Volume BF
     if (mi.in.get("NVBF") != null) {
        inNVBF = mi.in.get("NVBF") 
     } 
     
     // Percentage
     if (mi.in.get("PCTG") != null) {
        inPCTG = mi.in.get("PCTG") 
     } 
 
     // Effect on Inventory
     if (mi.in.get("EINV") != null) {
        inEINV = mi.in.get("EINV") 
     } 

     // Note
     if (mi.in.get("NOTE") != null) {
        inNOTE = mi.in.get("NOTE") 
     } else {
        inNOTE = ""       
     }
     

     // Validate reason record
     Optional<DBContainer> EXTIRP = findEXTIRP(inCONO, inDIVI, inRPID)
     if (!EXTIRP.isPresent()) {
        mi.error("Deck Register doesn't exist")   
        return             
     } else {
        // Update record
        updEXTIRPRecord()
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
  // Update EXTIRP record
  //********************************************************************    
  void updEXTIRPRecord(){      
     DBAction action = database.table("EXTIRP").index("00").build()
     DBContainer EXTIRP = action.getContainer()     
     EXTIRP.set("EXCONO", inCONO)
     EXTIRP.set("EXDIVI", inDIVI)
     EXTIRP.set("EXRPID", inRPID)

     // Read with lock
     action.readLock(EXTIRP, updateCallBackEXTIRP)
     }
   
     Closure<?> updateCallBackEXTIRP = { LockedResult lockedResult -> 
       if (inRPNA != null && inRPNA != "") {
          lockedResult.set("EXRPNA", inRPNA)
       }
       
        if (inRECD != null && inRECD != "") {
          lockedResult.set("EXRECD", inRECD)
       }
       
       if (inISPC != null) {
          lockedResult.set("EXISPC", inISPC)
       }
  
       if (inLOAD != null) {
          lockedResult.set("EXLOAD", inLOAD)
       }
   
       if (inDLOG != null) {
          lockedResult.set("EXDLOG", inDLOG)
       }
       
       if (inGVBF != null) {
          lockedResult.set("EXGVBF", inGVBF)
       }
       
       if (inNVBF != null) {
          lockedResult.set("EXNVBF", inNVBF)
       }
       
       if (inPCTG != null) {
          lockedResult.set("EXPCTG", inPCTG)
       }
  
       if (inEINV != null) {
          lockedResult.set("EXEINV", inEINV)
       }
  
       if (inNOTE != null) {
          lockedResult.set("EXNOTE", inNOTE)
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

