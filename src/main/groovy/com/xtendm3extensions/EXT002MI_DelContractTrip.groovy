// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to delete a contract from EXTCTT
// Transaction DelContractTrip
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: CTNO - Contract Number
 * @param: RVID - Revision ID
 * @param: DLFY - Deliver From Yard
 * @param: DLTY - Deliver To Yard
 * 
*/


 public class DelContractTrip extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    Integer inCONO
    String inDIVI
    int inCTNO  
    String inRVID
    String inDLFY
    String inDLTY

  // Constructor 
  public DelContractTrip(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
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

     // Contract Number
     if (mi.in.get("CTNO") != null) {
        inCTNO = mi.in.get("CTNO") 
     } else {
        inCTNO = 0      
     }
     
     // Revision ID
     if (mi.in.get("RVID") != null) {
        inRVID = mi.inData.get("RVID").trim() 
     } else {
        inRVID = ""      
     }

     // Deliver From Yard
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.inData.get("DLFY").trim() 
     } else {
        inDLFY = ""      
     }

     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.inData.get("DLTY").trim() 
     } else {
        inDLTY = ""      
     }


     // Validate contract trip
     Optional<DBContainer> EXTCTT = findEXTCTT(inCONO, inDIVI, inCTNO, inRVID, inDLFY, inDLTY)
     if(!EXTCTT.isPresent()){
        mi.error("Contract Number doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTCTTRecord() 
     } 
     
  }
 

  //******************************************************************** 
  // Get EXTCTT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTT(int CONO, String DIVI, int CTNO, String RVID, String DLFY, String DLTY){  
     DBAction query = database.table("EXTCTT").index("00").build()
     DBContainer EXTCTT = query.getContainer()
     EXTCTT.set("EXCONO", CONO)
     EXTCTT.set("EXDIVI", DIVI)
     EXTCTT.set("EXCTNO", CTNO)
     EXTCTT.set("EXRVID", RVID)
     EXTCTT.set("EXDLFY", DLFY)
     EXTCTT.set("EXDLTY", DLTY)
     
     if(query.read(EXTCTT))  { 
       return Optional.of(EXTCTT)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record in EXTCTT
  //******************************************************************** 
  void deleteEXTCTTRecord(){ 
     DBAction action = database.table("EXTCTT").index("00").build()
     DBContainer EXTCTT = action.getContainer()
     EXTCTT.set("EXCONO", inCONO) 
     EXTCTT.set("EXDIVI", inDIVI) 
     EXTCTT.set("EXCTNO", inCTNO)
     EXTCTT.set("EXRVID", inRVID)
     EXTCTT.set("EXDLFY", inDLFY)
     EXTCTT.set("EXDLTY", inDLTY)

     action.readLock(EXTCTT, deleterCallbackEXTCTT)
  }
    
  Closure<?> deleterCallbackEXTCTT = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  
  
 }