// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete a supplier brand from EXTSBR
// Transaction DelSupBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: BRND - Brand
 * 
*/


 public class DelSupBrand extends ExtendM3Transaction {
    private final MIAPI mi 
    private final DatabaseAPI database 
    private final ProgramAPI program
    private final LoggerAPI logger
  
    int inCONO
    String inSUNO
    String inBRND
    
    // Constructor 
    public DelSupBrand(MIAPI mi, DatabaseAPI database, ProgramAPI program, LoggerAPI logger) {
       this.mi = mi
       this.database = database 
       this.program = program
       this.logger = logger
    } 
      
    public void main() { 
       // Set Company Number
       inCONO = program.LDAZD.CONO as Integer
  
       // Supplier
       if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
          inSUNO = mi.inData.get("SUNO").trim() 
       } else {
          inSUNO = ""     
       }
  
       // Brand
       if (mi.in.get("BRND") != null && mi.in.get("BRND") != "") {
          inBRND = mi.inData.get("BRND").trim() 
       } else {
          inBRND = ""     
       }
  
       // Validate supplier brand record
       Optional<DBContainer> EXTSBR = findEXTSBR(inCONO, inSUNO, inBRND)
       if(!EXTSBR.isPresent()){
          mi.error("Supplier Brand doesn't exist")   
          return             
       } else {
          // Delete records 
          deleteEXTSBRRecord() 
       } 
       
    }
  
  
    //******************************************************************** 
    // Get EXTSBR record
    //******************************************************************** 
    private Optional<DBContainer> findEXTSBR(int CONO, String SUNO, String BRND){  
       DBAction query = database.table("EXTSBR").index("00").build()
       DBContainer EXTSBR = query.getContainer()
       EXTSBR.set("EXCONO", CONO)
       EXTSBR.set("EXSUNO", SUNO)
       EXTSBR.set("EXBRND", BRND)
       if(query.read(EXTSBR))  { 
         return Optional.of(EXTSBR)
       } 
    
       return Optional.empty()
    }
    
  
    //******************************************************************** 
    // Delete record from EXTSBR
    //******************************************************************** 
    void deleteEXTSBRRecord(){ 
       DBAction action = database.table("EXTSBR").index("00").build()
       DBContainer EXTSBR = action.getContainer()
       EXTSBR.set("EXCONO", inCONO) 
       EXTSBR.set("EXSUNO", inSUNO)
       EXTSBR.set("EXBRND", inBRND)
       action.readLock(EXTSBR, deleterCallbackEXTSBR)
    }
      
    Closure<?> deleterCallbackEXTSBR = { LockedResult lockedResult ->  
       lockedResult.delete()
    }
    

 }