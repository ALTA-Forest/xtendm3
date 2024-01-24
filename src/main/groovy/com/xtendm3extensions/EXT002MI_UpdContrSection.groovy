// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update contract header in EXTCDS
// Transaction UpdContrSection
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division Number
 * @param: DSID - Section ID
 * @param: CSNA - Section Name
 * @param: DPOR - Display Order
 * @param: CSSP - Species
 * @param: CSGR - Grades
 * @param: CSEX - Exception Codes
 * @param: CSLI - Length Increment
 * @param: FRSC - Frequency Scaler
*/



public class UpdContrSection extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inDSID
  String inCSNA
  int inDPOR
  int inCSSP
  int inCSGR
  int inCSEX
  int inCSLI
  int inFRSC
  String foundRVID
  
  // Constructor 
  public UpdContrSection(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Section Number
     if (mi.in.get("DSID") != null) {
        inDSID = mi.in.get("DSID") 
     } else {
        inDSID = 0      
     }

     // Section Name
     if (mi.inData.get("CSNA") != null) {
        inCSNA = mi.inData.get("CSNA").trim() 
     } else {
        inCSNA = ""        
     }
     
     // Display Order
     if (mi.in.get("DPOR") != null) {
        inDPOR = mi.in.get("DPOR") 
     }

     // Species
     if (mi.in.get("CSSP") != null) {
        inCSSP = mi.in.get("CSSP") 
     } 
     
     // Grades
     if (mi.in.get("CSGR") != null) {
        inCSGR = mi.in.get("CSGR") 
     } 
     
     // Exception Codes
     if (mi.in.get("CSEX") != null) {
        inCSEX = mi.in.get("CSEX") 
     }
 
     // Length Increment
     if (mi.in.get("CSLI") != null) {
        inCSLI = mi.in.get("CSLI") 
     }

     // Frequency Scaling
     if (mi.in.get("FRSC") != null) {
        inFRSC = mi.in.get("FRSC") 
     }
    
     findEXTCDSbyDSID()
     if (foundRVID != null && foundRVID != "") {    
        // Write record
        updEXTCDSRecord()
     } else {
        mi.error("Section doesn't exist")   
        return                    
     }
     
  }
  

  //******************************************************************** 
  // Get EXTCDS record by DSID
  //********************************************************************    
  void findEXTCDSbyDSID() {
    DBAction query = database.table("EXTCDS").index("20").selection("EXRVID").build()
    DBContainer EXTCDS = query.getContainer()
    EXTCDS.set("EXCONO", inCONO)
    EXTCDS.set("EXDIVI", inDIVI)
    EXTCDS.set("EXDSID", inDSID)
    query.readAll(EXTCDS, 3, releasedItemProcessor)
  }

  Closure<?> releasedItemProcessor = { DBContainer EXTCDS ->
    foundRVID = EXTCDS.getString("EXRVID")
  }  


  //******************************************************************** 
  // Update EXTCDS record
  //********************************************************************    
  void updEXTCDSRecord(){      
     DBAction action = database.table("EXTCDS").index("20").build()
     DBContainer EXTCDS = action.getContainer()   
     EXTCDS.set("EXCONO", inCONO)     
     EXTCDS.set("EXDIVI", inDIVI)  
     EXTCDS.set("EXDSID", inDSID)

     // Read with lock
     action.readAllLock(EXTCDS, 3, updateCallBackEXTCDS)
     }
   
     Closure<?> updateCallBackEXTCDS = { LockedResult lockedResult -> 
       if (inCSNA != null && inCSNA != "") {
          lockedResult.set("EXCSNA", inCSNA)
       }
  
       if (mi.in.get("DPOR") != null && mi.in.get("DPOR") != "") {
          lockedResult.set("EXDPOR", mi.in.get("DPOR"))
       }
      
       if (mi.in.get("CSSP") != null && mi.in.get("CSSP") != "") {
          lockedResult.set("EXCSSP", mi.in.get("CSSP"))
       }
  
       if (mi.in.get("CSGR") != null && mi.in.get("CSGR") != "") {
          lockedResult.set("EXCSGR", mi.in.get("CSGR"))
       }
       
       if (mi.in.get("CSEX") != null && mi.in.get("CSEX") != "") {
          lockedResult.set("EXCSEX", mi.in.get("CSEX"))
       }
  
       if (mi.in.get("CSLI") != null && mi.in.get("CSLI") != "") {
          lockedResult.set("EXCSLI", mi.in.get("CSLI"))
       }
   
       if (mi.in.get("FRSC") != null && mi.in.get("FRSC") != "") {
          lockedResult.set("EXFRSC", mi.in.get("FRSC"))
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

