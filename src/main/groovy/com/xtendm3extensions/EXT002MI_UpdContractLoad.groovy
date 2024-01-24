// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-07-06
// @version   1.0 
//
// Description 
// This API is to update section rate in EXTCTL
// Transaction UpdContractLoad
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division Number
 * @param: DSID - Section ID
 * @param: CRSQ - Rate Sequence
 * @param: CRML - Min Length
 * @param: CRXL - Max Length
 * @param: CRMD - Min Diameter
 * @param: CRXD - Max Diameter
 * @param: CRRA - Amount
 * @param: CRNO - Note
 * 
*/



public class UpdContractLoad extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDLNO
  int inCTNO
  String inRVID
  double inTNLB  
  double inTNBF  
  double inAVBL  

  
  // Constructor 
  public UpdContractLoad(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
    
     // Total Net lbs
     if (mi.in.get("TNLB") != null) {
        inTNLB = mi.in.get("TNLB") 
     } 

     // Total Net bf
     if (mi.in.get("TNBF") != null) {
        inTNBF = mi.in.get("TNBF") 
     } 

     // Average bf/lbs
     if (mi.in.get("AVBL") != null) {
        inAVBL = mi.in.get("AVBL") 
     } 
     
     // Validate Contract Load record
     Optional<DBContainer> EXTCTL = findEXTCTL(inCONO, inDIVI, inDLNO, inCTNO, inRVID)
     if(!EXTCTL.isPresent()){
        mi.error("Contract Load doesn't exists")   
        return             
     } else {
        // Write record
        updEXTCTLRecord()            
     }
     
  }
  
  //******************************************************************** 
  // Get EXTCTL record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCTL(int CONO, String DIVI, int DLNO, int CTNO, String RVID){  
     DBAction query = database.table("EXTCTL").index("00").build()
     def EXTCTL = query.getContainer()
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
  // Update EXTCTL record
  //********************************************************************    
  void updEXTCTLRecord(){      
     DBAction action = database.table("EXTCTL").index("00").build()
     DBContainer EXTCTL = action.getContainer()     
     EXTCTL.set("EXCONO", inCONO)     
     EXTCTL.set("EXDIVI", inDIVI)  
     EXTCTL.set("EXDLNO", inDLNO)
     EXTCTL.set("EXCTNO", inCTNO)
     EXTCTL.set("EXRVID", inRVID)

     // Read with lock
     action.readAllLock(EXTCTL, 5, updateCallBackEXTCTL)
     }
   
     Closure<?> updateCallBackEXTCTL = { LockedResult lockedResult -> 
       if (inTNLB != null && inTNLB != "") {
          lockedResult.set("EXTNLB", inTNLB)
       }
  
       if (inTNBF != null && inTNBF != "") {
          lockedResult.set("EXTNBF", inTNBF)
       }
       
       if (inAVBL != null && inAVBL != "") {  
          lockedResult.set("EXAVBL", inAVBL)
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

