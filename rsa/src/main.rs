use std::{fmt::{self}, str::FromStr, io, io::{Write}};
use colored::Colorize;
use num::{bigint::{BigUint, RandBigInt}, FromPrimitive, Integer, BigInt, One, Zero};
use num_prime::RandPrime;

#[derive(Debug, Clone)]
struct Key {
    pub n: BigUint,
    pub k: BigUint,
}

#[derive(Debug, Clone)]
struct KeyPair {
    pub name: String,
    pub public_key: Key,
    pub private_key: Key,
}

struct Crypt {}

impl Crypt {


    fn string_to_biguints(string: String) -> Vec<BigUint> {
        let bytes = string.as_bytes();
        let mut unencoded: Vec<BigUint> = Vec::new();
        for i in 0..bytes.len() {
            if i % 8 == 0 {
                unencoded.push(BigUint::from_u32(0).unwrap());
            }
            
            *unencoded.last_mut().unwrap() += BigUint::from_u64((bytes[i] as u64) << 8 * (i % 8)).unwrap();
        }

        unencoded
    }
    
    fn biguints_to_string(biguints: Vec<BigUint>) -> Result<String, Box<dyn std::error::Error>> {
        let mut chars: Vec<u8> = Vec::new();
    
        for num in biguints {
            let long_num: u64 = num.try_into()?;
    
            for character in long_num.to_le_bytes() {
                if character != 0 {
                    chars.push(character);
                }
            }
        }
    
        Ok(String::from_utf8(chars)?)
    }
    
    fn rsa_code_one(key: &Key, num: &BigUint) -> BigUint {
        num.modpow(&key.k, &key.n)
    }
    
    fn rsa_code(key: &Key, input: Vec<BigUint>) -> Vec<BigUint> {
        let mut coded: Vec<BigUint> = Vec::new();
        for num in input {
            coded.push(Crypt::rsa_code_one(key, &num));
        }
        coded
    }
    
    fn rsa_code_string(key: &Key, string: String) -> Vec<BigUint> {
        Crypt::rsa_code(key, Crypt::string_to_biguints(string))
    }
    
    fn from_encoded_format(string: String) -> Vec<BigUint> {
        let mut nums: Vec<BigUint> = Vec::new();
        let split = string.split(",");
    
        for strnum in split {
            nums.push(BigUint::from_str(strnum).unwrap());
        }
    
        nums
    }
    
    fn to_encoded_format(vec: Vec<BigUint>) -> String {
        let string = vec.iter().map(|x| x.to_string() + ",").collect::<String>();
        string[0..(string.len() - 1)].to_string()
    }
    
    fn encode_string(key: &Key, string: String) -> String {
        Crypt::to_encoded_format(Crypt::rsa_code_string(key, string))
    }
    
    fn decode_string(key: &Key, string: String) -> Result<String, Box<dyn std::error::Error>> {
        Crypt::biguints_to_string(Crypt::rsa_code(key, Crypt::from_encoded_format(string)))
    }
    
    fn modular_inverse(a: &BigUint, b: &BigUint) -> BigUint {
    
        let zero = BigInt::zero();
        let one = BigInt::one();
    
        let a_bigint: BigInt = a.to_owned().try_into().unwrap();
        let b_bigint: BigInt = b.to_owned().try_into().unwrap();
    
        let mut r_2 = b_bigint.clone();
        let mut r_1 = a_bigint.clone();
    
        let mut x_2 = zero.clone();
        let mut x_1 = one.clone();
    
        while r_1 != one {
    
            let q_0 = &r_2 / &r_1;
            let r_0 = &r_2 - (&q_0 * &r_1);
            let x_0 = &x_2 - (&q_0 * &x_1);
    
            r_2 = r_1;
            x_2 = x_1;
    
            r_1 = r_0;
            x_1 = x_0;
    
        }
    
        if x_1 >= zero {
            return x_1.to_owned().try_into().unwrap();
        } else {
            return (x_1 + b_bigint).try_into().unwrap();
        }
    
    }

    fn fermat(n: BigUint) -> (BigUint, BigUint) {

        let mut x = n.sqrt() + BigUint::one();
        let mut y = BigUint::zero();

        let mut square = &x * &x - &y * &y;
        let mut i = 0;
        let mut i_mil = 0;

        while square != n {
            if square < n {
                x += BigUint::one();
            }
            if square > n {
                y += BigUint::one();
            }
            square = &x * &x - &y * &y;

            i += 1;             
            if i == 1_000_000 {
                i_mil += 1;
                i = 0;
                println!("{}", format!("breaking... {} iterations...", format!("{} million", i_mil).red()).dimmed());
            }
            
        }

        (&x + &y, &x - &y)
    }

}



impl Key {

    fn new(n: BigUint, k: BigUint) -> Key {
        return Key { n, k };
    }

    fn from_string(s: String) -> Result<Key, Box<dyn std::error::Error>> {
        let components: Vec<&str> = s.split(";").collect();

        if components.len() != 3 {
            return Err(Box::new(std::io::Error::new(std::io::ErrorKind::InvalidInput, "Invalid arguments in key.")));
        }

        let n: BigUint = BigUint::from_str(components[1])?;
        let k: BigUint = BigUint::from_str(components[2])?;

        Ok(Key::new(n, k))
    }

}

impl fmt::Display for Key {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{};{};{}", self.n.bits(), self.n, self.k)
    }
}

impl KeyPair {

    fn new(name: String, public_key: Key, private_key: Key) -> KeyPair {
        return KeyPair { name, public_key, private_key };
    }

    fn random(name: String, size: usize) -> KeyPair {
        let mut random = rand::thread_rng();
    
        let p: BigUint = random.gen_prime(size / 2, None);
        let q: BigUint = random.gen_prime(size / 2, None);
    
        let n = &p * &q;
    
        let phi: BigUint = &n - &p - &q + &BigUint::one();
    
        let mut e: BigUint = random.gen_biguint_below(&phi);
        
        while &phi.gcd(&e) != &BigUint::one() {
            e = random.gen_biguint_below(&phi);
        }
    
        let d: BigUint = Crypt::modular_inverse(&e, &phi);
    
        let public_key = Key::new(n.clone(), e.clone());
        let private_key = Key::new(n.clone(), d.clone());
    
        KeyPair::new(name, public_key, private_key)
    }

    fn encrypt_with_public(&self, string: String) -> String {
        Crypt::encode_string(&self.public_key, string)
    }

    fn encrypt_with_private(&self, string: String) -> String {
        Crypt::encode_string(&self.private_key, string)
    }

    fn decrypt_with_public(&self, string: String) -> Result<String, Box<dyn std::error::Error>> {
        Crypt::decode_string(&self.public_key, string)
    }

    fn decrypt_with_private(&self, string: String) -> Result<String, Box<dyn std::error::Error>> {
        Crypt::decode_string(&self.private_key, string)
    }

}

impl fmt::Display for KeyPair {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "KeyPair {{ public_key: {}, private_key: {} }}", self.public_key, self.private_key)
    }
}

#[derive(PartialEq, Eq, PartialOrd, Ord)]
enum OperatorMenu {
    Home,
    SelectKey,
    PublicOrPrivate,
    Encrypt,
    Decrypt,
    AddSetPublicKey,
    AddSetPrivateKey,
    AddSetName,
    CreateSetBitSize,
    CreateSetName,
    BreakKey,
    GoHome,
}

struct Operator {
    menu: OperatorMenu,
    key_pairs: Vec<KeyPair>,
    key_pair_index: usize,
    page: usize,
    encrypting: bool,
    using_public: bool,
    temp_public_key: String,
    temp_private_key: String,
    temp_bitsize: usize,
    temp_name: String,
}

impl Operator {

    fn new(default_key_pair: KeyPair) -> Operator {
        Operator { 
            menu: OperatorMenu::Home, 
            key_pairs: vec![default_key_pair], 
            key_pair_index: 0,
            page: 0,
            encrypting: false,
            using_public: false,
            temp_public_key: "".to_string(),
            temp_private_key: "".to_string(),
            temp_bitsize: 0,
            temp_name: "".to_string(),
        }
    }

    fn input(&mut self, input: String) {
        if input == "0" {
            self.menu = OperatorMenu::Home;
            return;
        }

        match self.menu {
            OperatorMenu::Home => {
                match input.as_str() {
                    "1" => {
                        self.page = 0;
                        self.menu = OperatorMenu::SelectKey;
                    },
                    "2" => {
                        self.encrypting = true;
                        self.menu = OperatorMenu::PublicOrPrivate;
                    },
                    "3" => {
                        self.encrypting = false;
                        self.menu = OperatorMenu::PublicOrPrivate;
                    },
                    "4" => {
                        self.temp_private_key = "".to_string();
                        self.temp_public_key = "".to_string();
                        self.temp_name = "".to_string();
                        self.menu = OperatorMenu::AddSetPublicKey;
                    },
                    "5" => {
                        self.temp_bitsize = 0;
                        self.menu = OperatorMenu::CreateSetBitSize;
                    }
                    "6" => {
                        self.menu = OperatorMenu::BreakKey;
                    }
                    _ => {
                        println!("{}", "Invalid input!".red());
                    }
                }
            },
            OperatorMenu::SelectKey => {
                let parsed = input.parse();

                if parsed.is_err() {
                    println!("{}", "Invalid input!".red());
                    return;
                }

                let num: usize = parsed.unwrap();
                if num < 8 {
                    self.key_pair_index = num - 1 + self.page * 7;
                } else if num == 8 {
                    let maximum_pages = self.key_pairs.len() / 7;
                    if self.page < maximum_pages {
                        self.page += 1;
                    }
                } else if num == 9 {
                    if self.page > 0 {
                        self.page -= 1;
                    }
                }
            },
            OperatorMenu::PublicOrPrivate => {
                match input.as_str() {
                    "1" => {
                        self.using_public = true;
                        self.menu = if self.encrypting { OperatorMenu::Encrypt } else { OperatorMenu::Decrypt };
                    },
                    "2" => {
                        self.using_public = false;
                        self.menu = if self.encrypting { OperatorMenu::Encrypt } else { OperatorMenu::Decrypt };
                    }
                    _ => {
                        println!("{}", "Invalid input!".red());
                    }
                }
            },
            OperatorMenu::Encrypt => {
                let key_pair = &self.key_pairs[self.key_pair_index];

                println!("{}", "encrypting...".dimmed());

                let result = if self.using_public {
                    key_pair.encrypt_with_public(input)
                } else {
                    key_pair.encrypt_with_private(input)
                };

                println!("encrypted message: {}", result.green());

                self.menu = OperatorMenu::GoHome;
            },
            OperatorMenu::Decrypt => {
                let key_pair = &self.key_pairs[self.key_pair_index];

                println!("{}", "decrypting...".dimmed());
                
                let result = if self.using_public {
                    key_pair.decrypt_with_public(input)
                } else {
                    key_pair.decrypt_with_private(input)
                };

                if result.is_err() {
                    println!("{}", "error: could not be decrypted (did you use the right keypair?)".red());
                } else {
                    println!("decrypted message: {}", result.unwrap().blue());
                }

                self.menu = OperatorMenu::GoHome;
            }
            OperatorMenu::AddSetPublicKey => {
                self.temp_public_key = input;
                self.menu = OperatorMenu::AddSetPrivateKey;
            },
            OperatorMenu::AddSetPrivateKey => {
                self.temp_private_key = input;
                self.menu = OperatorMenu::AddSetName;
            },
            OperatorMenu::AddSetName => {
                self.temp_name = input;
                println!("{}", "creating key...".dimmed());

                let public_key = Key::from_string(self.temp_public_key.clone());
                let private_key = Key::from_string(self.temp_private_key.clone());

                if public_key.is_err() {
                    println!("{}", "error: error creating key".red());
                    return;
                }
                self.key_pairs.push(
                    KeyPair::new(self.temp_name.clone(), 
                        public_key.unwrap(), 
                        private_key.unwrap_or(Key::new(num::zero(), num::zero()))
                    )
                );

                println!("{} `{}`", "new key created:", self.temp_name.red());
                self.menu = OperatorMenu::GoHome;
            },
            OperatorMenu::CreateSetBitSize => {
                let bit_size = input.parse();

                if bit_size.is_err() {
                    println!("{}", "Invalid input!".red());
                    return;
                }

                self.temp_bitsize = bit_size.unwrap();
                self.menu = OperatorMenu::CreateSetName;
            },
            OperatorMenu::CreateSetName => {
                self.temp_name = input;
                println!("{}", "creating key...".dimmed());

                let new_key_pair = KeyPair::random(self.temp_name.clone(), self.temp_bitsize);

                println!("{} `{}`", "new key created:", self.temp_name.red());
                println!("public: {}", new_key_pair.public_key.to_string().blue());
                println!("private: {}", new_key_pair.private_key.to_string().green());

                self.key_pairs.push(new_key_pair);
                self.menu = OperatorMenu::GoHome;
            },
            OperatorMenu::GoHome => {
                self.menu = OperatorMenu::Home;
            },
            OperatorMenu::BreakKey => {
                let key_pair = &mut self.key_pairs[self.key_pair_index];

                println!("{}", "breaking key...".dimmed());

                let (p, q) = Crypt::fermat(key_pair.public_key.n.clone());
                println!("factor p: {}", p.to_string().blue());
                println!("factor q: {}", q.to_string().green());

                println!("{}", "generating private key...".dimmed());

                let phi = &key_pair.public_key.n - &p - &q + &BigUint::one();
                let d = Crypt::modular_inverse(&key_pair.public_key.k, &phi);

                key_pair.private_key = Key::new(key_pair.public_key.n.clone(), d.clone());

                println!("private key: {}", key_pair.private_key.to_string().red());
                println!("{}", "private key automatically set.".dimmed());

                self.menu = OperatorMenu::GoHome;
            }
        }
    }

    fn read_line(&self) -> io::Result<String> {
        let mut buffer = String::new();
        io::stdin().read_line(&mut buffer)?;
        Ok(buffer[0 .. buffer.len() - 2].to_string())
    }

    fn output(&self) {
        let current_key_pair = &self.key_pairs[self.key_pair_index];

        match self.menu {
            OperatorMenu::Home => {
                println!("[{}] {}: `{}`", "home".yellow(), "key pair".bright_cyan(), current_key_pair.name.red());
                println!("{}: {}", "1".blue(), "select key pair");
                println!("{}: {}", "2".blue(), "encrypt a message");
                println!("{}: {}", "3".blue(), "decrypt a message");
                println!("{}: {}", "4".blue(), "add key pair");
                println!("{}: {}", "5".blue(), "generate new key pair");
                println!("{}: {}", "6".blue(), "break key");
                println!("{}: {}", "7".blue(), "exit");
                print!("select menu: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::SelectKey => {
                println!("[{}] {}: `{}`", "key room".yellow(), "select a key. current key pair".bright_cyan(), current_key_pair.name.red());
                for i in 0..7 {
                    let index = self.page * 7 + i;
                    let key_pair_index_string = (i + 1).to_string();

                    if index < self.key_pairs.len() {
                        let key_pair = &self.key_pairs[index];
                        let key_pair_index_num = if index == self.key_pair_index { key_pair_index_string.bold().green() } else { key_pair_index_string.blue() };
                        println!("{}: {}", key_pair_index_num, key_pair.name);
                    } else {
                        let key_pair_index_string = (i + 1).to_string();
                        println!("{}: {}", key_pair_index_string.dimmed(), ".".dimmed());
                    }
                }

                let maximum_pages = self.key_pairs.len() / 7;
                println!("{}: {}", 
                    if self.page < maximum_pages { "8".blue() } else { "8".dimmed() }, 
                    if self.page < maximum_pages { "next page".white() } else { "next page".dimmed() }
                );
                println!("{}: {}", 
                    if self.page > 0 { "9".blue() } else { "9".dimmed() }, 
                    if self.page > 0 { "prev page".white() } else { "prev page".dimmed() }
                );
                println!("{}: {}", "0".blue(), "back home");
                print!("select: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::PublicOrPrivate => {
                println!("[{} {}] {}: `{}`", "select key type".yellow(), 
                    if self.encrypting { "(encrypting)".bright_blue().italic() } else { "(decrypting)".bright_red().italic() }, 
                    "pick a key type. current key pair".bright_cyan(), current_key_pair.name.red());
                println!("{}: {}", "1".blue(), "use public key");
                println!("{}: {}", "2".blue(), "use private key");
                println!("{}: {}", "0".blue(), "back home");
                print!("select: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::Encrypt => {
                println!("[{}] {}: `{}`", "encryption zone".yellow(), "current key pair".bright_cyan(), current_key_pair.name.red());
                print!("enter message (plaintext): ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::Decrypt => {
                println!("[{}] {}: `{}`", "decryption zone".yellow(), "current key pair".bright_cyan(), current_key_pair.name.red());
                print!("enter message (comma seperated numbers): ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::AddSetPublicKey => {
                println!("[{}] {}", "add new keypair".yellow(), "please enter the public key".bright_cyan());
                print!("public key (in standard format): ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::AddSetPrivateKey => {
                println!("[{}] {}", "add new keypair".yellow(), "please enter the private key (leave blank if unknown)".bright_cyan());
                print!("private key (in standard format): ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::AddSetName => {
                println!("[{}] {}", "add new keypair".yellow(), "please enter a human-readable name".bright_cyan());
                print!("name: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::GoHome => {
                print!("{}", "press enter to return home: ".dimmed());
                io::stdout().flush().unwrap();
            },
            OperatorMenu::CreateSetBitSize => {
                println!("[{}] {}", "create new keypair".yellow(), "enter bit size".bright_cyan());
                print!("bit size: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::CreateSetName => {
                println!("[{}] {}", "create new keypair".yellow(), "please enter a human-readable name".bright_cyan());
                print!("name: ");
                io::stdout().flush().unwrap();
            },
            OperatorMenu::BreakKey => {
                println!("[{}] {}: `{}`", "breaking key".yellow(), "current key pair".bright_cyan(), current_key_pair.name.red());
                print!("press enter to begin breaking: ");
                io::stdout().flush().unwrap();
            }
            // _ => {
            //     println!("[{}] menu is unfinished.", "unfinished".yellow());
            //     println!("{}: {}", "0".blue(), "back home");
            // }
        }
    }

    pub fn run(&mut self) {
        loop {
            self.output();
            let input = self.read_line().unwrap();
            if self.menu == OperatorMenu::Home && input.as_str() == "7" { break };
            self.input(input);
        }
    }
}

fn main() {

    let default_public_key: Key = Key::from_string("100;695277244306460071520774329847;239501611116209305167803678285".to_string()).unwrap();
    let default_private_key: Key = Key::from_string("100;695277244306460071520774329847;138209143200094300898126177525".to_string()).unwrap();
    let default_key_pair: KeyPair = KeyPair::new("default".to_string(), default_public_key, default_private_key);

    let mut operator = Operator::new(default_key_pair);
    operator.run();

}
