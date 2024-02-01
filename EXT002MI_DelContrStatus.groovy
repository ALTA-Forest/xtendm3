// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract status from EXTCTS
// Transaction DelContrStatus
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: SEQN - Sequence
 * 
*/


 public class DelContrStatus extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelContrStatus(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
  
       // Revision ID
       String inRVID      
       if (mi.in.get("RVID") != null) {
          inRVID = mi.in.get("RVID") 
       } else {
          inRVID = ""     
       }
  
       // Sequence
       int inSEQN      
       if (mi.in.get("SEQN") != null) {
          inSEQN = mi.in.get("SEQN") 
       } else {
          inSEQN = 0     
       }
  
       // Validate contract status record
       Optional<DBContainer> EXTCTS = findEXTCTS(inCONO, inDIVI, inRVID, inSEQN)
       if(!EXTCTS.isPresent()){
          mi.error("Contract Status doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCTSRecord(inCONO, inDIVI, inRVID, inSEQN) 
       } 
       
    }
  
  
    //******************************************************************** 
    // Get EXTCTS record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCTS(int CONO, String DIVI, String RVID, int SEQN){  
       DBAction query = database.table("EXTCTS").index("00").build()
       DBContainer EXTCTS = query.getContainer()
       EXTCTS.set("EXCONO", CONO)
       EXTCTS.set("EXDIVI", DIVI)
       EXTCTS.set("EXRVID", RVID)
       EXTCTS.set("EXSEQN", SEQN)
       
       if(query.read(EXTCTS))  { 
         return Optional.of(EXTCTS)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTCTS
    //******************************************************************** 
    void deleteEXTCTSRecord(int CONO, String DIVI, String RVID, int SEQN){ 
       DBAction action = database.table("EXTCTS").index("00").build()
       DBContainer EXTCTS = action.getContainer()
       EXTCTS.set("EXCONO", CONO)
       EXTCTS.set("EXDIVI", DIVI)
       EXTCTS.set("EXRVID", RVID)
       EXTCTS.set("EXSEQN", SEQN)
  
       action.readLock(EXTCTS, deleterCallbackEXTCTS)
    }
      
    Closure<?> deleterCallbackEXTCTS = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }