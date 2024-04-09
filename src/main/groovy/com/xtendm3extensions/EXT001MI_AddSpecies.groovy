// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add Species to EXTSPE
// Transaction AddSpecies
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



public class AddSpecies extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddSpecies(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int inCONO = program.LDAZD.CONO as Integer
     
     // Category
     String inCATE
     if (mi.in.get("CATE") != null && mi.in.get("CATE") != "") {
        inCATE = mi.inData.get("CATE").trim() 
     } else {
        inCATE = ""        
     }
     
     // Species
     String inSPEC
     if (mi.in.get("SPEC") != null && mi.in.get("SPEC") != "") {
        inSPEC = mi.inData.get("SPEC").trim() 
     } else {
        inSPEC = ""         
     }
      
    // Name
     String inSPNA
     if (mi.in.get("SPNA") != null && mi.in.get("SPNA") != "") {
        inSPNA = mi.inData.get("SPNA").trim() 
     } else {
        inSPNA = ""        
     }
     
     // Sort Code
     String inSORT
     if (mi.in.get("SORT") != null && mi.in.get("SORT") != "") {
        inSORT = mi.inData.get("SORT").trim() 
     } else {
        inSORT = ""       
     }


     // Validate species record
     Optional<DBContainer> EXTSPE = findEXTSPE(inCONO, inCATE, inSPEC)
     if(EXTSPE.isPresent()){
        mi.error("Species already exists")   
        return             
     } else {
        // Write record 
        addEXTSPERecord(inCONO, inCATE, inSPEC, inSPNA, inSORT)          
     }  

  }
  

  //******************************************************************** 
  // Get EXTSPE record
  //******************************************************************** 
  private Optional<DBContainer> findEXTSPE(int CONO, String CATE, String SPEC){  
     DBAction query = database.table("EXTSPE").index("00").build()
     DBContainer EXTSPE = query.getContainer()
     EXTSPE.set("EXCONO", CONO)
     EXTSPE.set("EXCATE", CATE)
     EXTSPE.set("EXSPEC", SPEC)
     if(query.read(EXTSPE))  { 
       return Optional.of(EXTSPE)
     } 
  
     return Optional.empty()
  }
  
  //******************************************************************** 
  // Add EXTSPE record 
  //********************************************************************     
  void addEXTSPERecord(int CONO, String CATE, String SPEC, String SPNA, String SORT){     
       DBAction action = database.table("EXTSPE").index("00").build()
       DBContainer EXTSPE = action.createContainer()
       EXTSPE.set("EXCONO", CONO)
       EXTSPE.set("EXCATE", CATE)
       EXTSPE.set("EXSPEC", SPEC)
       EXTSPE.set("EXSPNA", SPNA)
       EXTSPE.set("EXSORT", SORT)   
       EXTSPE.set("EXCHID", program.getUser())
       EXTSPE.set("EXCHNO", 1)         
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSPE.set("EXRGDT", regdate) 
       EXTSPE.set("EXLMDT", regdate) 
       EXTSPE.set("EXRGTM", regtime)
       action.insert(EXTSPE)         
 } 

     
} 

