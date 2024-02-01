// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to delete a contract load from EXTCTL
// Transaction DelContractLoad
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DLNO - Delivery Number
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * 
*/


 public class DelContractLoad extends ExtendM3Transaction {
    private final MIAPI mi
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI

  
  // Constructor 
  public DelContractLoad(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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

     // Contract Number
     int inCTNO
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }

     // Revision ID
     String inRVID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""      
     }


     // Validate contract load record
     Optional<DBContainer> EXTCTL = findEXTCTL(inCONO, inDIVI, inDLNO, inCTNO, inRVID)
     if(!EXTCTL.isPresent()){
        mi.error("Contract Load doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTCTLRecord(inCONO, inDIVI, inDLNO, inCTNO, inRVID) 
     } 
     
  }
 

  //******************************************************************** 
  // Get EXTCTL record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTL(int CONO, String DIVI, int DLNO, int CTNO, String RVID){  
     DBAction query = database.table("EXTCTL").index("00").build()
     DBContainer EXTCTL = query.getContainer()
     EXTCTL.set("EXCONO", CONO)
     EXTCTL.set("EXDIVI", DIVI)
     EXTCTL.set("EXDLNO", DLNO)
     EXTCTL.set("EXCTNO", CTNO)
     EXTCTL.set("EXRVID", RVID)
     if(query.read(EXTCTL))  { 
       return Optional.of(EXTCTL)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTCTL
  //******************************************************************** 
  void deleteEXTCTLRecord(int CONO, String DIVI, int DLNO, int CTNO, String RVID){ 
     DBAction action = database.table("EXTCTL").index("00").build()
     DBContainer EXTCTL = action.getContainer()
     EXTCTL.set("EXCONO", CONO)
     EXTCTL.set("EXDIVI", DIVI)
     EXTCTL.set("EXDLNO", DLNO)
     EXTCTL.set("EXCTNO", CTNO)
     EXTCTL.set("EXRVID", RVID)

     action.readLock(EXTCTL, deleterCallbackEXTCTL)
  }
    
  Closure<?> deleterCallbackEXTCTL = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }