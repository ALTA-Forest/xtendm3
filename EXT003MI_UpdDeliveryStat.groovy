// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update delivery status in EXTDLS
// Transaction UpdDeliveryStat
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STAT - Status
 * @param: MDUL - Module
 * @param: CRDT - Date Updated
 * @param: USID - Updated By
 * @param: NOTE - Note
*/



public class UpdDeliveryStat extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inSEQN
  int inSTAT
  String inMDUL
  int inCRDT
  String inUSID
  String inNOTE
  
  // Constructor 
  public UpdDeliveryStat(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Sequence
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0         
     }
                
     // Status
     if (mi.in.get("STAT") != null) {
        inSTAT = mi.in.get("STAT") 
     } else {
        inSTAT = 0      
     }

     // Module
     if (mi.in.get("MDUL") != null) {
        inMDUL = mi.in.get("MDUL") 
     } else {
        inMDUL = ""       
     }

     // Date Updated
     if (mi.in.get("CRDT") != null) {
        inCRDT = mi.in.get("CRDT") 
     } else {
        inCRDT = 0       
     }

     // Updated By
     if (mi.in.get("USID") != null) {
        inUSID = mi.in.get("USID") 
     } else {
        inUSID = ""       
     }

     // Comment
     if (mi.in.get("NOTE") != null) {
        inNOTE = mi.in.get("NOTE") 
     } else {
        inNOTE = ""       
     }


     // Validate Delivery Status record
     Optional<DBContainer> EXTDLS = findEXTDLS(inCONO, inDIVI, inDLNO, inSEQN)
     if (!EXTDLS.isPresent()) {
        mi.error("Delivery Status record doesn't exist")   
        return             
     } else {
        // Update record
        updEXTDLSRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTDLS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDLS(int CONO, String DIVI, int DLNO, int SEQN){  
     DBAction query = database.table("EXTDLS").index("00").build()
     DBContainer EXTDLS = query.getContainer()
     EXTDLS.set("EXCONO", CONO)
     EXTDLS.set("EXDIVI", DIVI)
     EXTDLS.set("EXDLNO", DLNO)
     EXTDLS.set("EXSEQN", SEQN)
     if(query.read(EXTDLS))  { 
       return Optional.of(EXTDLS)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTDLS record
  //********************************************************************    
  void updEXTDLSRecord(){      
     DBAction action = database.table("EXTDLS").index("00").build()
     DBContainer EXTDLS = action.getContainer()     
     EXTDLS.set("EXCONO", inCONO)
     EXTDLS.set("EXDIVI", inDIVI)
     EXTDLS.set("EXDLNO", inDLNO)
     EXTDLS.set("EXSEQN", inSEQN)

     // Read with lock
     action.readLock(EXTDLS, updateCallBackEXTDLS)
     }
   
     Closure<?> updateCallBackEXTDLS = { LockedResult lockedResult -> 
       if (inSTAT != 0) {
          lockedResult.set("EXSTAT", inSTAT)
       }
  
       if (inMDUL != "") {
          lockedResult.set("EXMDUL", inMDUL)
       }
   
        if (inCRDT != 0) {
          lockedResult.set("EXCRDT", inCRDT)
       }
  
       if (inUSID != "") {
          lockedResult.set("EXUSID", inUSID)
       }
  
       if (inNOTE != "") {
          lockedResult.set("EXNOTE", inNOTE)
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

