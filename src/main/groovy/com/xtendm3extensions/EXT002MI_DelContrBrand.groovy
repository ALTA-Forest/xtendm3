// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a contract brand from EXTCTB
// Transaction DelContrBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CONO - Company Number
 * @param: DIVI - Division
 * @param: RVID - Revision ID
 * @param: BRND - Brand
 * 
*/


 public class DelContrBrand extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
    
    Integer inCONO
    String inDIVI

  
    // Constructor 
    public DelContrBrand(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
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
       if (mi.in.get("RVID") != null) {
          inRVID = mi.in.get("RVID") 
       } else {
          inRVID = ""      
       }
  
       // Brand
       String inBRND      
       if (mi.in.get("BRND") != null) {
          inBRND = mi.in.get("BRND") 
       } else {
          inBRND = ""     
       }
  
  
       // Validate contract brand record
       Optional<DBContainer> EXTCTB = findEXTCTB(inCONO, inDIVI, inRVID, inBRND)
       if(!EXTCTB.isPresent()){
          mi.error("Contract Brand doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTCTBRecord(inCONO, inDIVI, inRVID, inBRND) 
       } 
       
    }
   
  
    //******************************************************************** 
    // Get EXTCTB record
    //******************************************************************** 
    private Optional<DBContainer> findEXTCTB(int CONO, String DIVI, String RVID, String BRND){  
       DBAction query = database.table("EXTCTB").index("00").build()
       DBContainer EXTCTB = query.getContainer()
       EXTCTB.set("EXCONO", CONO)
       EXTCTB.set("EXDIVI", DIVI)
       EXTCTB.set("EXRVID", RVID)
       EXTCTB.set("EXBRND", BRND)
       if(query.read(EXTCTB))  { 
         return Optional.of(EXTCTB)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTCTB
    //******************************************************************** 
    void deleteEXTCTBRecord(int CONO, String DIVI, String RVID, String BRND){ 
       DBAction action = database.table("EXTCTB").index("00").build()
       DBContainer EXTCTB = action.getContainer()
       EXTCTB.set("EXCONO", CONO)
       EXTCTB.set("EXDIVI", DIVI)
       EXTCTB.set("EXRVID", RVID)
       EXTCTB.set("EXBRND", BRND)
  
       action.readLock(EXTCTB, deleterCallbackEXTCTB)
    }
      
    Closure<?> deleterCallbackEXTCTB = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }