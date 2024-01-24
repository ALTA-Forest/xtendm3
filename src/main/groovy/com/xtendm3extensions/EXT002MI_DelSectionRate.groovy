// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract section rate from EXTCSR
// Transaction DelSectionRate
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * 
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: CRSQ - Rate Sequence
 * 
*/


 public class DelSectionRate extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelSectionRate(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
  
       // Section ID
       int inDSID      
       if (mi.in.get("DSID") != null) {
          inDSID = mi.in.get("DSID") 
       } else {
          inDSID = 0     
       }
       
       // Rate Sequence
       int inCRSQ      
       if (mi.in.get("CRSQ") != null) {
          inCRSQ = mi.in.get("CRSQ") 
       } else {
          inCRSQ = 0     
       }
  
       // Validate contract section rate record
       Optional<DBContainer> EXTCSR = findEXTCSR(inCONO, inDIVI, inDSID, inCRSQ)
       if(!EXTCSR.isPresent()){
          mi.error("Contract Section Rate doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCSRRecord(inCONO, inDIVI, inDSID, inCRSQ) 
       } 
       
    }
  
  
  
    //******************************************************************** 
    // Get EXTCSR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCSR(int CONO, String DIVI, int DSID, int CRSQ){  
       DBAction query = database.table("EXTCSR").index("00").build()
       DBContainer EXTCSR = query.getContainer()
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
    // Delete record from EXTCSR
    //******************************************************************** 
    void deleteEXTCSRRecord(int CONO, String DIVI, int DSID, int CRSQ){ 
       DBAction action = database.table("EXTCSR").index("00").build()
       DBContainer EXTCSR = action.getContainer()
       EXTCSR.set("EXCONO", CONO)
       EXTCSR.set("EXDIVI", DIVI)
       EXTCSR.set("EXDSID", DSID)
       EXTCSR.set("EXCRSQ", CRSQ)
  
       action.readLock(EXTCSR, deleterCallbackEXTCSR)
    }
      
    Closure<?> deleterCallbackEXTCSR = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }