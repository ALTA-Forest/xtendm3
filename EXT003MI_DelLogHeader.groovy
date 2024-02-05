// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to delete log header from EXTSLH
// Transaction DelLogHeader
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Sclae Ticket ID
 * @param: SEQN - Log Number
 * 
*/


 public class DelLogHeader extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelLogHeader(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Scale Ticket ID
     int inSTID     
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } else {
        inSTID = 0     
     }

     // Log Number
     int inSEQN      
     if (mi.in.get("SEQN") != null) {
        inSEQN = mi.in.get("SEQN") 
     } else {
        inSEQN = 0     
     }


     // Validate log header record
     Optional<DBContainer> EXTSLH = findEXTSLH(inCONO, inDIVI, inSTID, inSEQN)
     if(!EXTSLH.isPresent()){
        mi.error("Log Header doesn't exist")   
        return             
     } else {
        // Delete record 
        deleteEXTSLHRecord(inCONO, inDIVI, inSTID, inSEQN) 
     } 
     
  }


  //******************************************************************** 
  // Get EXTSLH record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSLH(int CONO, String DIVI, int STID, int SEQN){  
     DBAction query = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = query.getContainer()
     EXTSLH.set("EXCONO", CONO)
     EXTSLH.set("EXDIVI", DIVI)
     EXTSLH.set("EXSTID", STID)
     EXTSLH.set("EXSEQN", SEQN)
     if(query.read(EXTSLH))  { 
       return Optional.of(EXTSLH)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTSLH
  //******************************************************************** 
  void deleteEXTSLHRecord(int CONO, String DIVI, int STID, int SEQN){ 
     DBAction action = database.table("EXTSLH").index("00").build()
     DBContainer EXTSLH = action.getContainer()
     EXTSLH.set("EXCONO", CONO)
     EXTSLH.set("EXDIVI", DIVI)
     EXTSLH.set("EXSTID", STID)
     EXTSLH.set("EXSEQN", SEQN)

     action.readLock(EXTSLH, deleterCallbackEXTSLH)
  }
    
  Closure<?> deleterCallbackEXTSLH = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }