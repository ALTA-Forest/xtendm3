// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract payee from EXTCTP
// Transaction DelContrPayee
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * 
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CASN - Payee Number
 * @param: CF15 - Payee Role
 * 
*/


 public class DelContrPayee extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelContrPayee(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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
       if (mi.in.get("RVID") != null && mi.in.get("RVID") != "") {
          inRVID = mi.inData.get("RVID").trim() 
       } else {
          inRVID = ""     
       }
  
       // Payee ID
       int inCPID     
       if (mi.in.get("CPID") != null) {
          inCPID = mi.in.get("CPID") 
       } else {
          inCPID = 0     
       }
  
  
       // Validate contract payee record
       Optional<DBContainer> EXTCTP = findEXTCTP(inCONO, inDIVI, inRVID, inCPID)
       if(!EXTCTP.isPresent()){
          mi.error("Contract Payee doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCTPRecord(inCONO, inDIVI, inRVID, inCPID) 
       } 
       
    }
  
  
    //******************************************************************** 
    // Get EXTCTP record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCTP(int CONO, String DIVI, String RVID, int CPID){  
       DBAction query = database.table("EXTCTP").index("00").build()
       DBContainer EXTCTP = query.getContainer()
       EXTCTP.set("EXCONO", CONO)
       EXTCTP.set("EXDIVI", DIVI)
       EXTCTP.set("EXRVID", RVID)
       EXTCTP.set("EXCPID", CPID)
       
       if(query.read(EXTCTP))  { 
         return Optional.of(EXTCTP)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTCTP
    //******************************************************************** 
    void deleteEXTCTPRecord(int CONO, String DIVI, String RVID, int CPID){ 
       DBAction action = database.table("EXTCTP").index("00").build()
       DBContainer EXTCTP = action.getContainer()
       EXTCTP.set("EXCONO", CONO)
       EXTCTP.set("EXDIVI", DIVI)
       EXTCTP.set("EXRVID", RVID)
       EXTCTP.set("EXCPID", CPID)
  
       action.readLock(EXTCTP, deleterCallbackEXTCTP)
    }
      
    Closure<?> deleterCallbackEXTCTP = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }