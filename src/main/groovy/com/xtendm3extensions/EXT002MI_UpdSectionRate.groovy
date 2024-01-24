// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update section rate in EXTCSR
// Transaction UpdSectionRate
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



public class UpdSectionRate extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDSID
  int inCRSQ 
  double inCRML  
  double inCRXL  
  double inCRMD  
  double inCRXD  
  double inCRRA  
  String inCRNO

  
  // Constructor 
  public UpdSectionRate(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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
         
     // Section ID
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     } else {
        inDSID = 0        
     }
     
     // Rate Sequence
     if (mi.in.get("CRSQ") != null) {
        inCRSQ = mi.in.get("CRSQ") 
     } 

     // Min Length
     if (mi.in.get("CRML") != null) {
        inCRML = mi.in.get("CRML") 
     } 

     // Max Length
     if (mi.in.get("CRXL") != null) {
        inCRXL = mi.in.get("CRXL") 
     } 

     // Min Diameter
     if (mi.in.get("CRMD") != null) {
        inCRMD = mi.in.get("CRMD") 
     } 

     // Max Diameter
     if (mi.in.get("CRXD") != null) {
        inCRXD = mi.in.get("CRXD") 
     } 

     // Amount
     if (mi.in.get("CRRA") != null) {
        inCRRA = mi.in.get("CRRA") 
     }

     // Note
     if (mi.in.get("CRNO") != null) {
        inCRNO = mi.in.get("CRNO") 
     } 

     
     // Validate Contract Section Rate record
     Optional<DBContainer> EXTCSR = findEXTCSR(inCONO, inDIVI, inDSID, inCRSQ)
     if(!EXTCSR.isPresent()){
        mi.error("Contract Section Rate doesn't exists")   
        return             
     } else {
        // Write record
        updEXTCSRRecord()            
     }
     
  }
  
  //******************************************************************** 
  // Get EXTCSR record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCSR(int CONO, String DIVI, int DSID, int CRSQ){  
     DBAction query = database.table("EXTCSR").index("00").build()
     def EXTCSR = query.getContainer()
     EXTCSR.set("EXCONO", CONO)
     EXTCSR.set("EXDIVI", DIVI)
     EXTCSR.set("EXDSID", DSID)
     EXTCSR.set("EXCRSQ", CRSQ)
     if(query.read(EXTCSR))  { 
       return Optional.of(EXTCSR)
     } 
  
     return Optional.empty()
  }


  //******************************************************************** 
  // Update EXTCDS record
  //********************************************************************    
  void updEXTCSRRecord(){      
     DBAction action = database.table("EXTCSR").index("00").build()
     DBContainer EXTCSR = action.getContainer()     
     EXTCSR.set("EXCONO", inCONO)     
     EXTCSR.set("EXDIVI", inDIVI)  
     EXTCSR.set("EXDSID", inDSID)
     EXTCSR.set("EXCRSQ", inCRSQ)

     // Read with lock
     action.readAllLock(EXTCSR, 4, updateCallBackEXTCSR)
     }
   
     Closure<?> updateCallBackEXTCSR = { LockedResult lockedResult -> 
       if (inCRML != null && inCRML != "") {
          lockedResult.set("EXCRML", inCRML)
       }
  
       if (inCRXL != null && inCRXL != "") {
          lockedResult.set("EXCRXL", inCRXL)
       }
       
       if (inCRMD != null && inCRMD != "") {  
          lockedResult.set("EXCRMD", inCRMD)
       }
  
       if (inCRXD != null && inCRXD != "") {
          lockedResult.set("EXCRXD", inCRXD)
       }
  
       if (inCRRA != null && inCRRA != "") {
          lockedResult.set("EXCRRA", inCRRA)
       }
  
       if (inCRRA != null && inCRRA != "") {
          lockedResult.set("EXCRNO", inCRNO)
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

