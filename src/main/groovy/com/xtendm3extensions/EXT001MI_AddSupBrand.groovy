// @author    Jessica Bjorklund (jessica.bjorklund@columbusglobal.com)
// @date      2023-04-10
// @version   1.0 
//
// Description 
// This API is to add a Supplier Brand to EXTSBR
// Transaction AddSupBrand
// AFMNI-7/Alias Replacement
// https://leanswift.atlassian.net/browse/AFMI-7

/**
 * IN
 * @param: SUNO - Supplier
 * @param: BRND - Brand
 * 
*/


public class AddSupBrand extends ExtendM3Transaction {
  private final MIAPI mi 
  private final DatabaseAPI database
  private final ProgramAPI program
  private final UtilityAPI utility
  private final LoggerAPI logger
  
  // Constructor 
  public AddSupBrand(MIAPI mi, DatabaseAPI database, UtilityAPI utility, ProgramAPI program, LoggerAPI logger) {
     this.mi = mi
     this.database = database
     this.utility = utility
     this.program = program
     this.logger = logger
  } 
    
  public void main() {       
     // Set Company Number
     int inCONO = program.LDAZD.CONO as Integer

     // Supplier
     String inSUNO
     if (mi.in.get("SUNO") != null && mi.in.get("SUNO") != "") {
        inSUNO = mi.inData.get("SUNO").trim() 
        
        // Validate supplier if entered
        Optional<DBContainer> CIDMAS = findCIDMAS(inCONO, inSUNO)
        if (!CIDMAS.isPresent()) {
           mi.error("Supplier doesn't exist")   
           return             
        }

     } else {
        inSUNO = ""         
     }

     // Brand
     String inBRND
     if (mi.in.get("BRND") != null && mi.in.get("BRND") != "") {
        inBRND = mi.inData.get("BRND").trim() 
     } else {
        inBRND = ""        
     }
     
      
     // Validate supplier brand record
     Optional<DBContainer> EXTSBR = findEXTSBR(inCONO, inSUNO, inBRND)
     if(EXTSBR.isPresent()){
        mi.error("Supplier Brand already exists")   
        return             
     } else {
        // Write record 
        addEXTSBRRecord(inCONO, inSUNO, inBRND)          
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
   // Check Supplier
   //******************************************************************** 
   private Optional<DBContainer> findCIDMAS(int CONO, String SUNO){  
     DBAction query = database.table("CIDMAS").index("00").build()   
     DBContainer CIDMAS = query.getContainer()
     CIDMAS.set("IDCONO", CONO)
     CIDMAS.set("IDSUNO", SUNO)
    
     if(query.read(CIDMAS))  { 
       return Optional.of(CIDMAS)
     } 
  
     return Optional.empty()
   }

  
  //******************************************************************** 
  // Add EXTSBR record 
  //********************************************************************     
  void addEXTSBRRecord(int CONO, String SUNO, String BRND){     
       DBAction action = database.table("EXTSBR").index("00").build()
       DBContainer EXTSBR = action.createContainer()
       EXTSBR.set("EXCONO", CONO)
       EXTSBR.set("EXSUNO", SUNO)
       EXTSBR.set("EXBRND", BRND)   
       EXTSBR.set("EXCHID", program.getUser())
       EXTSBR.set("EXCHNO", 1)          
       int regdate = utility.call("DateUtil", "currentDateY8AsInt")
       int regtime = utility.call("DateUtil", "currentTimeAsInt")                    
       EXTSBR.set("EXRGDT", regdate) 
       EXTSBR.set("EXLMDT", regdate) 
       EXTSBR.set("EXRGTM", regtime)
       action.insert(EXTSBR)         
 } 

     
} 

