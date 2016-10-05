Vagrant.require_version ">= 1.6.5"

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/xenial64"

  config.vm.define "web1"

  config.vm.network "forwarded_port", guest: 8070, host:8000
  config.vm.network "forwarded_port", guest: 80, host:8001

  # Disable the new default behavior introduced in Vagrant 1.7, to
  # ensure that all Vagrant machines will use the same SSH key pair.
  # See https://github.com/mitchellh/vagrant/issues/5005
#  config.ssh.insert_key = false

  #
  # This should resolv guest dns calls
  #
  config.vm.provider "virtualbox" do |v| 
    v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
  end

  #
  # Ubuntu doesn't get dns server adresses
  #
  config.vm.provision "shell" do |s|
    s.inline = 'sudo bash -c "echo nameserver 8.8.8.8 >/etc/resolv.conf"'
  end

  #
  # We currently need python2 to run ansible
  #
  config.vm.provision "shell" do |s|
    s.inline = "apt-get install -y python"
  end

  #
  # Run Ansible from the Vagrant Host
  #
  config.vm.provision "ansible" do |ansible|
    ansible.verbose = "v"
    ansible.playbook = "playbook.yml"
    ansible.sudo = true
    ansible.groups = {
      "webservers" => ["web1"]
    }
  end
end

