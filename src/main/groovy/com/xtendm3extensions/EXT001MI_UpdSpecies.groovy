// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to update species in EXTSPE
// Transaction UpdSpecies
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: CATE - Category
 * @param: SPEC - Species
 * @param: SPNA - Name
 * @param: SORT - Sort Code
 * 
*/


public class UpdSpecies extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  int inCONO
  String inCATE
  String inSPEC
  String inSPNA
  String inSORT
  
  
  // Constructor 
  public UpdSpecies(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger     
  } 
    
  public void main() {       
     // Set Company Number
     inCONO = program.LDAZD.CONO as Integer

     // Category
     if (mi.inData.get("CATE") != null) {
        inCATE = mi.inData.get("CATE").trim() 
     } else {
        inCATE = ""        
     }

     // Species
     if (mi.inData.get("SPEC") != null) {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""         
     }
            
     // Name
     if (mi.inData.get("SPNA") != null) {
        inSPNA = mi.inData.get("SPNA").trim() 
     } else {
        inSPNA = ""        
     }

     // Sort Code
     if (mi.inData.get("SORT") != null) {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT = ""       
     }


     // Validate species record
     Optional<DBContainer> EXTSPE = findEXTSPE(inCONO, inCATE, inSPEC)
     if(!EXTSPE.isPresent()){
        mi.error("Species doesn't exist")   
        return             
     }     
    
     // Update record
     updEXTSPERecord()
     
  }
  
    
  //******************************************************************** 
  // Get EXTSPE record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSPE(int cono, String cate, String spec){  
     DBAction query = database.table("EXTSPE").index("00").build()
     def EXTSPE = query.getContainer()
     EXTSPE.set("EXCONO", cono)
     EXTSPE.set("EXCATE", cate)
     EXTSPE.set("EXSPEC", spec)
     if(query.read(EXTSPE))  { 
       return Optional.of(EXTSPE)
     } 
  
     return Optional.empty()
  }
  

  //******************************************************************** 
  // Update EXTSPE record
  //********************************************************************    
  void updEXTSPERecord(){      
     DBAction action = database.table("EXTSPE").index("00").build()
     DBContainer EXTSPE = action.getContainer()    
     EXTSPE.set("EXCONO", inCONO)    
     EXTSPE.set("EXCATE", inCATE)
     EXTSPE.set("EXSPEC", inSPEC)

     // Read with lock
     action.readLock(EXTSPE, updateCallBackEXTSPE)
     }
   
     Closure<?> updateCallBackEXTSPE = { LockedResult lockedResult -> 

     if (inSPNA == "?") {
        lockedResult.set("EXSPNA", "")
     } else {     
        if (inSPNA != "") {
           lockedResult.set("EXSPNA", inSPNA)
        }
     }

     if (inSORT == "?") {
        lockedResult.set("EXSORT", "")
     } else {
        if (inSORT != "") {
           lockedResult.set("EXSORT", inSORT)
        }
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

