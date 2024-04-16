// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract section from EXTCDS
// Transaction DelContrSection
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 *  
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: CSSN - Section Number
 * 
*/


 public class DelContrSection extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
  // Constructor 
  public DelContrSection(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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

     // Section Number
     int inCSSN      
     if (mi.in.get("CSSN") != null) {
        inCSSN = mi.in.get("CSSN") 
     } else {
        inCSSN = 0     
     }

     // Validate contract section record
     Optional<DBContainer> EXTCDS = findEXTCDS(inCONO, inDIVI, inRVID, inCSSN)
     if(!EXTCDS.isPresent()){
        mi.error("Contract Status doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTCDSRecord(inCONO, inDIVI, inRVID, inCSSN) 
     } 
     
  }



  //******************************************************************** 
  // Get EXTCTS record
  //******************************************************************** 
  private Optional<DBContainer> findEXTCDS(int CONO, String DIVI, String RVID, int CSSN){  
     DBAction query = database.table("EXTCDS").index("00").build()
     DBContainer EXTCDS = query.getContainer()
     EXTCDS.set("EXCONO", CONO)
     EXTCDS.set("EXDIVI", DIVI)
     EXTCDS.set("EXRVID", RVID)
     EXTCDS.set("EXCSSN", CSSN)
     
     if(query.read(EXTCDS))  { 
       return Optional.of(EXTCDS)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTCDS
  //******************************************************************** 
  void deleteEXTCDSRecord(int CONO, String DIVI, String RVID, int CSSN){ 
     DBAction action = database.table("EXTCDS").index("00").build()
     DBContainer EXTCDS = action.getContainer()
     EXTCDS.set("EXCONO", CONO)
     EXTCDS.set("EXDIVI", DIVI)
     EXTCDS.set("EXRVID", RVID)
     EXTCDS.set("EXCSSN", CSSN)

     action.readLock(EXTCDS, deleterCallbackEXTCDS)
  }
    
  Closure<?> deleterCallbackEXTCDS = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }