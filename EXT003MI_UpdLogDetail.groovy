// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-05-10
// @version   1.0 
//
// Description 
// This API is to update a log details in EXTSLD
// Transaction UpdLogDetail
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: STID - Scale Ticket ID
 * @param: LDID - Log Detail ID
 * @param: LGID - Log ID
 * @param: GRAD - Grade
 * @param: LLEN - Length
 * @param: LEND - Length Deduction
 * @param: LSDI - Small Diameter
 * @param: LLDI - Large Diameter
 * @param: DIAD - Diameter Deduction
 * @param: LGRV - Gross Volume
 * @param: LNEV - Net Volume
*/



public class UpdLogDetail extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final MICallerAPI miCaller
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  Integer inCONO
  String inDIVI
  int inSTID
  int inLDID
  int inLGID
  String inGRAD  
  double inLLEN 
  double inLEND 
  double inLSDI  
  double inLLDI
  double inDIAD
  double inLGRV
  double inLNEV

  
  // Constructor 
  public UpdLogDetail(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, ProgramAPI program, UtilityAPI utility, LoggerAPI logger) {
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

     // Scale Ticket ID
     if (mi.in.get("STID") != null) {
        inSTID = mi.in.get("STID") 
     } 

     // Log Detail ID
     if (mi.in.get("LDID") != null) {
        inLDID = mi.in.get("LDID") 
     }

     // Log ID
     if (mi.in.get("LGID") != null) {
        inLGID = mi.in.get("LGID") 
     }

     // Grade
     if (mi.in.get("GRAD") != null) {
        inGRAD = mi.in.get("GRAD") 
     } 
           
     // Length
     if (mi.in.get("LLEN") != null) {
        inLLEN = mi.in.get("LLEN") 
     }
 
      // Length Deduction
     if (mi.in.get("LEND") != null) {
        inLEND = mi.in.get("LEND") 
     }
     
     // Small Diameter
     if (mi.in.get("LSDI") != null) {
        inLSDI = mi.in.get("LSDI") 
     } 

     // Large Diameter
     if (mi.in.get("LLDI") != null) {
        inLLDI = mi.in.get("LLDI") 
     } 

     // Diameter Deduction
     if (mi.in.get("DIAD") != null) {
        inDIAD = mi.in.get("DIAD") 
     } 

     // Gross Volume
     if (mi.in.get("LGRV") != null) {
        inLGRV = mi.in.get("LGRV") 
     }

     // Net Volume
     if (mi.in.get("LNEV") != null) {
        inLNEV = mi.in.get("LNEV") 
     } 


     // Validate Log Detail Line record
     Optional<DBContainer> EXTSLD = findEXTSLD(inCONO, inDIVI, inSTID, inLDID)
     if (!EXTSLD.isPresent()) {
        mi.error("Log Detail doesn't exist")   
        return             
     } else {
        // Update record
        updEXTSLDRecord()
     }
     
  }
  
    
  //******************************************************************** 
  // Get EXTSLD record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSLD(int CONO, String DIVI, int STID, int LDID){  
     DBAction query = database.table("EXTSLD").index("00").build()
     DBContainer EXTSLD = query.getContainer()
     EXTSLD.set("EXCONO", CONO)
     EXTSLD.set("EXDIVI", DIVI)
     EXTSLD.set("EXSTID", STID)
     EXTSLD.set("EXLDID", LDID)
     if(query.read(EXTSLD))  { 
       return Optional.of(EXTSLD)
     } 
  
     return Optional.empty()
  }

  //******************************************************************** 
  // Update EXTSLD record
  //********************************************************************    
  void updEXTSLDRecord(){      
     DBAction action = database.table("EXTSLD").index("00").build()
     DBContainer EXTSLD = action.getContainer()     
     EXTSLD.set("EXCONO", inCONO)
     EXTSLD.set("EXDIVI", inDIVI)
     EXTSLD.set("EXSTID", inSTID)
     EXTSLD.set("EXLDID", inLDID)

     // Read with lock
     action.readLock(EXTSLD, updateCallBackEXTSLD)
     }
   
     Closure<?> updateCallBackEXTSLD = { LockedResult lockedResult -> 
       if (inGRAD != null && inGRAD != "") {
          lockedResult.set("EXGRAD", inGRAD)
       }
  
       if (inLLEN != null && inLLEN != "") {
          lockedResult.set("EXLLEN", inLLEN)
       }
   
       if (inLEND != null && inLEND != "") {
          lockedResult.set("EXLEND", inLEND)
       }
       
       if (inLSDI != null && inLSDI != "") {
          lockedResult.set("EXLSDI", inLSDI)
       }
  
       if (inLLDI != null && inLLDI != "") {
          lockedResult.set("EXLLDI", inLLDI)
       }
  
       if (inDIAD != null && inDIAD != "") {
          lockedResult.set("EXDIAD", inDIAD)
       }
  
       if (inLGRV != null && inLGRV != "") {
          lockedResult.set("EXLGRV", inLGRV)
       }
  
       if (inLNEV != null && inLNEV != "") {
          lockedResult.set("EXLNEV", inLNEV)
       }
  
      
       // Update changed information
       int changeNo = lockedResult.get("EXCHNO")
       int newChangeNo = changeNo + 1 
       int changedate = utility.call("DateUtil", "currentDateY8AsInt")
       lockedResult.set("EXLMDT", changedate)          
       lockedResult.set("EXCHNO", newChangeNo) 
       lockedResult.set("EXCHID", program.getUser())
       lockedResult.update()
    }

} 

