// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-09-10
// @version   1.0 
//
// Description 
// This API is to update a contract payee in EXTCTT
// Transaction UpdContractTrip
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
 * @param: TRRA - Trip Rate
 * @param: MTRA - Minimum Amount
 * 
*/



public class UpdContractTrip extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  String inRVID
  int inCTNO
  String inDLFY
  String inDLTY
  double inTRRA
  double inMTRA  
  
  // Constructor 
  public UpdContractTrip(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.miCaller = miCaller
     this.program = program
     this.utility = utility
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     if (inCONO == null || inCONO == 0) {
        inCONO = program.LDAZD.CONO as Integer
     } 

     // Set Division
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
        inRVID = mi.in.get("RVID") 
     } else {
        inRVID = ""         
     }

     // Deliver From Yard
     if (mi.in.get("DLFY") != null) {
        inDLFY = mi.in.get("DLFY") 
     } else {
        inDLFY = ""         
     }
     
     // Deliver To Yard
     if (mi.in.get("DLTY") != null) {
        inDLTY = mi.in.get("DLTY") 
     } else {
        inDLTY = ""         
     }
 
     // Trip Rate
     if (mi.in.get("TRRA") != null) {
        inTRRA = mi.in.get("TRRA") 
     } 
 
     // Minimum Amount
     if (mi.in.get("MTRA") != null) {
        inMTRA = mi.in.get("MTRA") 
     }

     // Validate contract trip record
     Optional<DBContainer> EXTCTT = findEXTCTT(inCONO, inDIVI, inCTNO, inRVID, inDLFY, inDLTY)
     if(!EXTCTT.isPresent()){
        mi.error("Contract Trip doesn't exist")   
        return             
     }     
    
     // Update record
     updEXTCTTRecord()
     
  }
  
    
  //******************************************************************** 
  // Get EXTCTT record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTT(int CONO, String DIVI, int CTNO, String RVID, String DLFY, String DLTY){  
     DBAction query = database.table("EXTCTT").index("00").build()
     def EXTCTT = query.getContainer()
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
  // Update EXTCTT record
  //********************************************************************    
  void updEXTCTTRecord(){      
     DBAction action = database.table("EXTCTT").index("00").build()
     DBContainer EXTCTT = action.getContainer()     
     EXTCTT.set("EXCONO", inCONO)
     EXTCTT.set("EXDIVI", inDIVI)
     EXTCTT.set("EXCTNO", inCTNO)
     EXTCTT.set("EXRVID", inRVID)
     EXTCTT.set("EXDLFY", inDLFY)
     EXTCTT.set("EXDLTY", inDLTY)

     // Read with lock
     action.readLock(EXTCTT, updateCallBackEXTCTT)
     }
   
     Closure<?> updateCallBackEXTCTT = { LockedResult lockedResult -> 
       if (mi.in.get("TRRA") != null) {
          lockedResult.set("EXTRRA", mi.in.get("TRRA"))
       }
       
       if (mi.in.get("MTRA") != null) {
          lockedResult.set("EXMTRA", mi.in.get("MTRA"))
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

