// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to delete species from EXTSPE
// Transaction DelSpecies
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CATE - Category
 * @param: SPEC - Species
 * 
*/

 import java.time.LocalDateTime;  
 import java.time.format.DateTimeFormatter;

 public class DelSpecies extends ExtendM3Transaction {
    private final MIAPI mi; 
    private final DatabaseAPI database; 
    private final ProgramAPI program;
    private final LoggerAPI logger;
  
    int CONO
    String inCATE
    String inSPEC

  // Constructor 
  public DelSpecies(MIAPI mi, DatabaseAPI database,ProgramAPI program, LoggerAPI logger) {
     this.mi = mi;
     this.database = database; 
     this.program = program;
     this.logger = logger;
  } 
    
  public void main() { 
     // Set Company Number
     CONO = program.LDAZD.CONO as Integer

     // Category
     if (mi.in.get("CATE") != null) {
        inCATE = mi.in.get("CATE") 
     } else {
        inCATE = ""     
     }

     // Species
     if (mi.in.get("SPEC") != null) {
        inSPEC = mi.in.get("SPEC") 
     } else {
        inSPEC = ""     
     }


     // Validate species record
     Optional<DBContainer> EXTSPE = findEXTSPE(CONO, inCATE, inSPEC)
     if(!EXTSPE.isPresent()){
        mi.error("Species doesn't exist")   
        return             
     } else {
        // Delete records 
        deleteEXTSPERecord() 
     } 
     
  }

  //******************************************************************** 
  // Validate String
  //******************************************************************** 
  public  boolean isNullOrEmpty(String key) {
      if(key != null && !key.isEmpty())
         return false;
      return true;
  }
    

  //******************************************************************** 
  // Get EXTSPE record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSPE(int CONO, String CATE, String SPEC){  
     DBAction query = database.table("EXTSPE").index("00").selection("EXCONO", "EXCATE", "EXSPEC").build()
     def EXTSPE = query.getContainer()
     EXTSPE.set("EXCONO", CONO)
     EXTSPE.set("EXCATE", CATE)
     EXTSPE.set("EXSPEC", SPEC)
     if(query.read(EXTSPE))  { 
       return Optional.of(EXTSPE)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Delete record from EXTSPE
  //******************************************************************** 
  void deleteEXTSPERecord(){ 

     DBAction action = database.table("EXTSPE").index("00").selectAllFields().build()
     DBContainer EXTSPE = action.getContainer()
     EXTSPE.set("EXCONO", CONO) 
     EXTSPE.set("EXCATE", inCATE)
     EXTSPE.set("EXSPEC", inSPEC)

     action.readLock(EXTSPE, deleterCallbackEXTSPE)
  }
    
  Closure<?> deleterCallbackEXTSPE = { LockedResult lockedResult ->  
     lockedResult.delete()
  }
  

 }