// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract section species from EXTCSS
// Transaction DelSectionSpec
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * 
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: DSID - Section ID
 * @param: SPEC - Species
 * 
*/


 public class DelSectionSpec extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI
  
    // Constructor 
    public DelSectionSpec(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
       
       // Species
       String inSPEC      
       if (mi.in.get("SPEC") != null) {
          inSPEC = mi.in.get("SPEC") 
       } else {
          inSPEC = ""     
       }
  
       // Validate contract section grade record
       Optional<DBContainer> EXTCSS = findEXTCSS(inCONO, inDIVI, inDSID, inSPEC)
       if(!EXTCSS.isPresent()){
          mi.error("Contract Section Species doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCSSRecord(inCONO, inDIVI, inDSID, inSPEC) 
       } 
       
    }
  
  
  
    //******************************************************************** 
    // Get EXTCSS record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCSS(int CONO, String DIVI, int DSID, String SPEC){  
       DBAction query = database.table("EXTCSS").index("00").build()
       DBContainer EXTCSS = query.getContainer()
       EXTCSS.set("EXCONO", CONO)
       EXTCSS.set("EXDIVI", DIVI)
       EXTCSS.set("EXDSID", DSID)
       EXTCSS.set("EXSPEC", SPEC)
       
       if(query.read(EXTCSS))  { 
         return Optional.of(EXTCSS)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTCSS
    //******************************************************************** 
    void deleteEXTCSSRecord(int CONO, String DIVI, int DSID, String SPEC){ 
       DBAction action = database.table("EXTCSS").index("00").build()
       DBContainer EXTCSS = action.getContainer()
       EXTCSS.set("EXCONO", CONO)
       EXTCSS.set("EXDIVI", DIVI)
       EXTCSS.set("EXDSID", DSID)
       EXTCSS.set("EXSPEC", SPEC)
  
       action.readLock(EXTCSS, deleterCallbackEXTCSS)
    }
      
    Closure<?> deleterCallbackEXTCSS = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }