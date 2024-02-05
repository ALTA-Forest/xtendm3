// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to delete a payee split from EXTDPS
// Transaction DelPayeeSplit
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: STID - Scale Ticket ID
 * @param: SEQN - Sequence
 * 
*/


 public class DelPayeeSplit extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI

  
  // Constructor 
  public DelPayeeSplit(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Delivery Number
     int inDLNO
     if (mi.in.get("DLNO") != null) {
        inDLNO = mi.in.get("DLNO") 
     } else {
        inDLNO = 0      
     }

     // Scale Ticket ID
     int inSTID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0      
     }
     
     // Item Number
     String inITNO
     if (mi.in.get("ITNO") != null) {
        inITNO = mi.in.get("ITNO") 
     } else {
        inITNO = ""      
     }

     // Sequence
     int inSEQN
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0      
     }



     // Validate payee split record
     Optional<DBContainer> EXTDPS = findEXTDPS(inCONO, inDIVI, inDLNO, inSTID, inITNO, inSEQN)
     if(!EXTDPS.isPresent()){
        mi.error("Payee Split doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTDPSRecord(inCONO, inDIVI, inDLNO, inSTID, inITNO, inSEQN) 
     } 
     
  }
 

  //******************************************************************** 
  // Get EXTDPS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTDPS(int CONO, String DIVI, int DLNO, int STID, String ITNO, int SEQN){  
     DBAction query = database.table("EXTDPS").index("00").build()
     DBContainer EXTDPS = query.getContainer()
     EXTDPS.set("EXCONO", CONO)
     EXTDPS.set("EXDIVI", DIVI)
     EXTDPS.set("EXDLNO", DLNO)
     EXTDPS.set("EXSTID", STID)
     EXTDPS.set("EXITNO", ITNO)
     EXTDPS.set("EXSEQN", SEQN)
     if(query.read(EXTDPS))  { 
       return Optional.of(EXTDPS)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTDPS
  //******************************************************************** 
  void deleteEXTDPSRecord(int CONO, String DIVI, int DLNO, int STID, String ITNO, int SEQN){ 
     DBAction action = database.table("EXTDPS").index("00").build()
     DBContainer EXTDPS = action.getContainer()
     EXTDPS.set("EXCONO", CONO)
     EXTDPS.set("EXDIVI", DIVI)
     EXTDPS.set("EXDLNO", DLNO)
     EXTDPS.set("EXSTID", STID)
     EXTDPS.set("EXITNO", ITNO)
     EXTDPS.set("EXSEQN", SEQN)

     action.readLock(EXTDPS, deleterCallbackEXTDPS)
  }
    
  Closure<?> deleterCallbackEXTDPS = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }